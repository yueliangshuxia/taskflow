// ===== TaskFlow Notification System =====

document.addEventListener('DOMContentLoaded', function() {
    // Initial fetch
    fetchUnreadCount();
    fetchRecentNotifications();

    // Poll every 30 seconds
    setInterval(fetchUnreadCount, 30000);
    setInterval(fetchRecentNotifications, 30000);

    // Fetch notifications when dropdown is shown
    var notificationDropdown = document.getElementById('notificationDropdown');
    if (notificationDropdown) {
        notificationDropdown.addEventListener('shown.bs.dropdown', function() {
            fetchRecentNotifications();
        });
    }
});

// Fetch unread count from server
function fetchUnreadCount() {
    var headers = {};
    headers[getCsrfHeader()] = getCsrfToken();

    fetch('/api/notifications/count', {
        method: 'GET',
        headers: headers
    })
    .then(function(response) {
        if (!response.ok) throw new Error('Failed to fetch unread count');
        return response.json();
    })
    .then(function(data) {
        updateBadge(data.count);
    })
    .catch(function(error) {
        console.error('Notification count error:', error);
    });
}

// Fetch recent notifications
function fetchRecentNotifications() {
    var listEl = document.getElementById('notificationList');
    var emptyEl = document.getElementById('notificationEmpty');
    var loadingEl = document.getElementById('notificationLoading');
    var markAllBtn = document.getElementById('markAllReadBtn');

    if (!listEl) return;

    // Show loading state
    if (emptyEl) emptyEl.style.display = 'none';
    if (loadingEl) loadingEl.style.display = 'block';
    if (markAllBtn) markAllBtn.style.display = 'none';

    var headers = {};
    headers[getCsrfHeader()] = getCsrfToken();

    fetch('/api/notifications/recent', {
        method: 'GET',
        headers: headers
    })
    .then(function(response) {
        if (!response.ok) throw new Error('Failed to fetch notifications');
        return response.json();
    })
    .then(function(notifications) {
        if (loadingEl) loadingEl.style.display = 'none';
        renderNotifications(notifications);
    })
    .catch(function(error) {
        console.error('Fetch notifications error:', error);
        if (loadingEl) loadingEl.style.display = 'none';
        if (emptyEl) {
            emptyEl.style.display = 'block';
            emptyEl.innerHTML = '<i class="fas fa-exclamation-circle fa-2x mb-2 d-block"></i>加载失败';
        }
    });
}

// Render notification items in the dropdown
function renderNotifications(notifications) {
    var listEl = document.getElementById('notificationList');
    var emptyEl = document.getElementById('notificationEmpty');
    var markAllBtn = document.getElementById('markAllReadBtn');

    if (!listEl) return;

    // Remove old items (keep empty and loading states)
    var items = listEl.querySelectorAll('.notification-item');
    items.forEach(function(el) { el.remove(); });

    if (!notifications || notifications.length === 0) {
        if (emptyEl) {
            emptyEl.style.display = 'block';
            emptyEl.innerHTML = '<i class="fas fa-inbox fa-2x mb-2 d-block"></i>暂无通知';
        }
        if (markAllBtn) markAllBtn.style.display = 'none';
        return;
    }

    if (emptyEl) emptyEl.style.display = 'none';
    if (markAllBtn) markAllBtn.style.display = 'inline-block';

    var hasUnread = false;

    notifications.forEach(function(notif) {
        var item = document.createElement('a');
        item.className = 'notification-item d-flex align-items-start gap-2';
        if (!notif.read) {
            item.classList.add('unread');
            hasUnread = true;
        }
        item.href = getNotificationLink(notif);
        item.setAttribute('data-id', notif.id);
        if (!notif.read) {
            item.addEventListener('click', function(e) {
                markAsRead(notif.id);
            });
        }

        var iconColor = getNotificationIconColor(notif.type);
        var iconClass = getNotificationIconClass(notif.type);

        item.innerHTML =
            '<div class="notification-icon" style="background: ' + iconColor + '20; color: ' + iconColor + ';">' +
                '<i class="fas ' + iconClass + '"></i>' +
            '</div>' +
            '<div class="flex-grow-1 min-width-0">' +
                '<div class="notification-text">' + escapeHtml(notif.message) + '</div>' +
                '<div class="notification-time">' + formatTime(notif.createdAt) + '</div>' +
            '</div>';

        listEl.appendChild(item);
    });

    updateBadge(hasUnread ? notifications.filter(function(n) { return !n.read; }).length : 0);
}

// Mark a single notification as read
function markAsRead(id) {
    var headers = {};
    headers[getCsrfHeader()] = getCsrfToken();
    headers['Content-Type'] = 'application/json';

    fetch('/api/notifications/' + id + '/read', {
        method: 'POST',
        headers: headers
    })
    .then(function(response) {
        if (response.ok) {
            // Update the UI: remove unread styling
            var item = document.querySelector('.notification-item[data-id="' + id + '"]');
            if (item) {
                item.classList.remove('unread');
            }
            fetchUnreadCount();
        }
    })
    .catch(function(error) {
        console.error('Mark as read error:', error);
    });
}

// Mark all notifications as read
function markAllAsRead() {
    var headers = {};
    headers[getCsrfHeader()] = getCsrfToken();
    headers['Content-Type'] = 'application/json';

    fetch('/api/notifications/read-all', {
        method: 'POST',
        headers: headers
    })
    .then(function(response) {
        if (response.ok) {
            // Remove all unread styling
            var items = document.querySelectorAll('.notification-item.unread');
            items.forEach(function(el) { el.classList.remove('unread'); });

            var markAllBtn = document.getElementById('markAllReadBtn');
            if (markAllBtn) markAllBtn.style.display = 'none';

            updateBadge(0);
            showToast('所有通知已标记为已读', 'success');
        }
    })
    .catch(function(error) {
        console.error('Mark all as read error:', error);
    });
}

// Update the bell badge
function updateBadge(count) {
    var badge = document.getElementById('notificationBadge');
    if (!badge) return;

    if (count > 0) {
        badge.style.display = 'inline';
        badge.textContent = count > 99 ? '99+' : count;
    } else {
        badge.style.display = 'none';
    }
}

// Get notification link based on entity type
function getNotificationLink(notif) {
    if (notif.relatedEntityType === 'Task' && notif.relatedEntityId) {
        return '/tasks/' + notif.relatedEntityId;
    }
    if (notif.relatedEntityType === 'Project' && notif.relatedEntityId) {
        return '/projects/' + notif.relatedEntityId;
    }
    return '#';
}

// Get icon class for notification type
function getNotificationIconClass(type) {
    switch (type) {
        case 'TASK_ASSIGNED':    return 'fa-user-plus';
        case 'TASK_COMMENTED':   return 'fa-comment';
        case 'STATUS_CHANGED':   return 'fa-arrow-right';
        case 'TASK_UPDATED':     return 'fa-pen';
        default:                 return 'fa-bell';
    }
}

// Get icon color for notification type
function getNotificationIconColor(type) {
    switch (type) {
        case 'TASK_ASSIGNED':    return '#4f46e5';
        case 'TASK_COMMENTED':   return '#06b6d4';
        case 'STATUS_CHANGED':   return '#10b981';
        case 'TASK_UPDATED':     return '#f59e0b';
        default:                 return '#64748b';
    }
}

// Format time for display
function formatTime(dateTimeStr) {
    if (!dateTimeStr) return '';
    try {
        var date = new Date(dateTimeStr);
        var now = new Date();
        var diff = now - date;
        var minutes = Math.floor(diff / 60000);
        var hours = Math.floor(diff / 3600000);
        var days = Math.floor(diff / 86400000);

        if (minutes < 1) return '刚刚';
        if (minutes < 60) return minutes + '分钟前';
        if (hours < 24) return hours + '小时前';
        if (days < 7) return days + '天前';

        var month = (date.getMonth() + 1).toString().padStart(2, '0');
        var day = date.getDate().toString().padStart(2, '0');
        var hour = date.getHours().toString().padStart(2, '0');
        var min = date.getMinutes().toString().padStart(2, '0');
        return month + '-' + day + ' ' + hour + ':' + min;
    } catch (e) {
        return '';
    }
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}
