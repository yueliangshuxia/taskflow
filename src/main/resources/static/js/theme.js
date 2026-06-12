/**
 * ThemeManager — Dark mode toggle for TaskFlow
 *
 * - Persists the user's choice in localStorage('theme')
 * - Reads the saved preference on page load
 * - Falls back to 'light' if nothing is stored
 * - Toggles data-theme="dark" on <html>
 */

const ThemeManager = (function () {
  const STORAGE_KEY = 'theme';
  const THEME_DARK = 'dark';
  const THEME_LIGHT = 'light';

  function getStoredTheme() {
    try {
      return localStorage.getItem(STORAGE_KEY);
    } catch (e) {
      return null;
    }
  }

  function setStoredTheme(theme) {
    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch (e) {
      // localStorage unavailable — skip
    }
  }

  function applyTheme(theme) {
    const root = document.documentElement;
    if (theme === THEME_DARK) {
      root.setAttribute('data-theme', THEME_DARK);
    } else {
      root.removeAttribute('data-theme');
    }
    updateToggleIcon(theme);
  }

  function updateToggleIcon(theme) {
    const toggle = document.getElementById('themeToggle');
    if (!toggle) return;
    const icon = toggle.querySelector('i');
    if (!icon) return;
    if (theme === THEME_DARK) {
      icon.className = 'fas fa-sun';
      toggle.setAttribute('title', '切换到亮色模式');
    } else {
      icon.className = 'fas fa-moon';
      toggle.setAttribute('title', '切换到暗色模式');
    }
  }

  function toggleTheme() {
    const current = getStoredTheme() || THEME_LIGHT;
    const next = current === THEME_DARK ? THEME_LIGHT : THEME_DARK;
    setStoredTheme(next);
    applyTheme(next);
  }

  function load() {
    const stored = getStoredTheme();
    const theme = stored === THEME_DARK ? THEME_DARK : THEME_LIGHT;
    applyTheme(theme);

    // Re-read the toggle icon after DOM is settled (in case the toggle button
    // was injected after this script ran)
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', function () {
        updateToggleIcon(getStoredTheme() || THEME_LIGHT);
      });
    } else {
      updateToggleIcon(theme);
    }
  }

  // Expose publicly
  return {
    load: load,
    toggle: toggleTheme
  };
})();

// Legacy function export for inline onclick="toggleTheme()" usage
function toggleTheme() {
  ThemeManager.toggle();
}
