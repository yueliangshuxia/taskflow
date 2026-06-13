/**
 * Vue 3 实用工具模块
 * 为 Thymeleaf 渐进增强提供统一的 Vue 应用管理
 * 依赖: Vue 3 (CDN), CSRF meta tags (base.html)
 */

(function() {
    'use strict';

    if (typeof Vue === 'undefined') {
        console.error('vue-utils: Vue 3 is not loaded');
        return;
    }

    const { createApp, ref, reactive, computed, onMounted, onUnmounted, nextTick } = Vue;

    // --- CSRF ---

    /**
     * 从 meta 标签读取 CSRF 令牌
     */
    function getCsrfToken() {
        var meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : '';
    }

    function getCsrfHeader() {
        var meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
    }

    /**
     * CSRF composable — 用于 Vue 组件
     * 返回 { csrfToken, csrfHeader, csrfHeaders }
     * csrfHeaders: 可直接传递给 fetch headers 的对象
     */
    function useCsrf() {
        var token = ref(getCsrfToken());
        var header = ref(getCsrfHeader());

        var csrfHeaders = computed(function() {
            var h = {};
            h[header.value] = token.value;
            return h;
        });

        return {
            csrfToken: token,
            csrfHeader: header,
            csrfHeaders: csrfHeaders
        };
    }

    // --- i18n ---

    /**
     * i18n composable
     * 读取 window.__messages (由 Thymeleaf 在 base.html 中注入)
     */
    function useI18n() {
        var messages = window.__messages || {};

        function t(key, fallback) {
            return messages[key] || fallback || key;
        }

        return { t: t, messages: messages };
    }

    // --- API ---

    /**
     * API composable — 封装 fetch 调用
     * 自动处理 CSRF、JSON 序列化、错误处理
     */
    function useApi() {
        var loading = ref(false);
        var error = ref(null);

        var csrf = useCsrf();

        /**
         * 发起 API 请求
         * @param {string} url — 请求 URL
         * @param {object} opts — { method?, body?, params?, onSuccess?, onError? }
         * @returns {Promise<any>} 响应数据
         */
        async function request(url, opts) {
            opts = opts || {};
            loading.value = true;
            error.value = null;

            var method = (opts.method || 'GET').toUpperCase();
            var headers = {
                'Accept': 'application/json'
            };

            // 构建查询参数
            var fullUrl = url;
            if (opts.params) {
                var qs = Object.keys(opts.params)
                    .filter(function(k) { return opts.params[k] !== null && opts.params[k] !== undefined && opts.params[k] !== ''; })
                    .map(function(k) { return encodeURIComponent(k) + '=' + encodeURIComponent(opts.params[k]); })
                    .join('&');
                if (qs) fullUrl += (url.indexOf('?') === -1 ? '?' : '&') + qs;
            }

            // POST/PUT/PATCH/DELETE 需要 CSRF
            if (method !== 'GET') {
                Object.assign(headers, csrf.csrfHeaders.value);
            }

            if (opts.body && typeof opts.body === 'object' && !(opts.body instanceof FormData)) {
                headers['Content-Type'] = 'application/json';
            }

            try {
                var response = await fetch(fullUrl, {
                    method: method,
                    headers: headers,
                    body: opts.body instanceof FormData ? opts.body
                        : opts.body ? JSON.stringify(opts.body) : undefined
                });

                if (!response.ok) {
                    var errData = null;
                    try { errData = await response.json(); } catch(e) { /* ignore */ }
                    var errMsg = (errData && errData.message) || 'Request failed: ' + response.status;
                    throw new Error(errMsg);
                }

                // 204 No Content
                if (response.status === 204) {
                    loading.value = false;
                    if (opts.onSuccess) opts.onSuccess(null);
                    return null;
                }

                var data = await response.json();
                loading.value = false;
                if (opts.onSuccess) opts.onSuccess(data);
                return data;

            } catch (err) {
                error.value = err.message;
                loading.value = false;
                if (opts.onError) opts.onError(err.message);
                throw err;
            }
        }

        return {
            loading: loading,
            error: error,
            get: function(url, opts) { return request(url, Object.assign({}, opts, { method: 'GET' })); },
            post: function(url, body, opts) { return request(url, Object.assign({}, opts, { method: 'POST', body: body })); },
            put: function(url, body, opts) { return request(url, Object.assign({}, opts, { method: 'PUT', body: body })); },
            patch: function(url, body, opts) { return request(url, Object.assign({}, opts, { method: 'PATCH', body: body })); },
            del: function(url, opts) { return request(url, Object.assign({}, opts, { method: 'DELETE' })); }
        };
    }

    // --- Toast 通知 ---

    function useToast() {
        function show(message, type) {
            type = type || 'success';
            if (typeof window.showToast === 'function') {
                window.showToast(message, type);
                return;
            }
            // 如果 showToast 不存在，创建临时 toast
            var container = document.getElementById('toast-container');
            if (!container) {
                container = document.createElement('div');
                container.id = 'toast-container';
                container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
                document.body.appendChild(container);
            }
            var toastEl = document.createElement('div');
            toastEl.className = 'toast align-items-center text-white bg-' + type + ' border-0 show';
            toastEl.setAttribute('role', 'alert');
            toastEl.innerHTML = '<div class="d-flex"><div class="toast-body">' + message + '</div>' +
                '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>';
            container.appendChild(toastEl);
            setTimeout(function() {
                toastEl.remove();
            }, 4000);
        }

        return {
            success: function(msg) { show(msg, 'success'); },
            error: function(msg) { show(msg, 'danger'); },
            warning: function(msg) { show(msg, 'warning'); },
            info: function(msg) { show(msg, 'info'); }
        };
    }

    // --- 应用创建器 ---

    /**
     * 创建并挂载一个 Vue 应用
     * @param {string|Element} selector — CSS 选择器或 DOM 元素
     * @param {object} componentOptions — Vue 组件选项
     * @returns {object} Vue 应用实例
     *
     * 用法:
     *   createVueApp('#my-widget', {
     *     setup() {
     *       const { t } = useI18n();
     *       const count = ref(0);
     *       return { count, t };
     *     },
     *     template: \`<div>{{ t('hello') }} {{ count }}</div>\`
     *   });
     */
    function createVueApp(selector, componentOptions) {
        var el = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (!el) {
            console.warn('vue-utils: Element not found:', selector);
            return null;
        }

        var app = createApp(componentOptions);
        app.mount(el);
        return app;
    }

    // --- 全局注册 (供内联模板使用) ---

    window.VueUtils = {
        createVueApp: createVueApp,
        useCsrf: useCsrf,
        useI18n: useI18n,
        useApi: useApi,
        useToast: useToast,
        ref: ref,
        reactive: reactive,
        computed: computed,
        onMounted: onMounted,
        onUnmounted: onUnmounted,
        nextTick: nextTick
    };

    // 保持与现有 main.js 全局函数的兼容
    window.getCsrfToken = window.getCsrfToken || getCsrfToken;
    window.getCsrfHeader = window.getCsrfHeader || getCsrfHeader;

})();
