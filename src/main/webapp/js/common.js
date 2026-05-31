/**
 * 公共 JS 工具库
 * 所有页面通用
 */
(function() {
    'use strict';

    /**
     * 封装 fetch 请求，自动添加 CSRF Token
     * @param {string} url - 请求地址
     * @param {object} options - fetch 选项
     * @returns {Promise}
     */
    window.apiFetch = function(url, options) {
        options = options || {};
        options.headers = options.headers || {};
        // 标记 AJAX 请求，后端 Filter 依此返回 JSON 而非 302 重定向
        if (!options.headers['X-Requested-With']) {
            options.headers['X-Requested-With'] = 'XMLHttpRequest';
        }
        var csrfToken = window._pageConfig && window._pageConfig.csrfToken;
        if (csrfToken && !options.headers['X-CSRF-Token']) {
            options.headers['X-CSRF-Token'] = csrfToken;
        }
        options.credentials = options.credentials || 'same-origin';
        return fetch(url, options);
    };

    /**
     * 显示 Toast 提示（替代 alert）
     * @param {string} message - 提示内容
     * @param {string} type - 类型: success / error / warning / info
     * @param {number} duration - 显示时长(毫秒)
     */
    window.showToast = function(message, type, duration) {
        type = type || 'info';
        duration = duration || 3000;

        var container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.style.cssText = 'position:fixed;top:20px;right:20px;z-index:10000;';
            document.body.appendChild(container);
        }

        var colorMap = {
            success: '#10B981',
            error: '#EF4444',
            warning: '#F59E0B',
            info: '#3B82F6'
        };

        var toast = document.createElement('div');
        toast.style.cssText = 'background:' + (colorMap[type] || colorMap.info) +
            ';color:#fff;padding:12px 20px;border-radius:6px;margin-bottom:8px;' +
            'font-size:14px;box-shadow:0 2px 8px rgba(0,0,0,0.2);' +
            'opacity:0;transform:translateX(100%);transition:all 0.3s;max-width:360px;word-break:break-word;';
        toast.textContent = message;
        container.appendChild(toast);

        // 动画入场
        requestAnimationFrame(function() {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(0)';
        });

        setTimeout(function() {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(function() {
                if (toast.parentNode) toast.parentNode.removeChild(toast);
            }, 300);
        }, duration);
    };

    /**
     * AJAX 表单提交 — 不跳转，原地 Toast 反馈
     * @param {HTMLFormElement} formEl - 表单元素
     * @param {object} opts - 选项
     * @param {function} opts.onSuccess - 成功回调(data)
     * @param {function} opts.onError - 失败回调(data)
     * @param {string} opts.successMsg - 成功提示文字
     * @param {string} opts.errorMsg - 失败提示文字
     */
    window.ajaxSubmit = function(formEl, opts) {
        opts = opts || {};
        var method = (formEl.method || 'POST').toUpperCase();
        var actionAttr = formEl.getAttribute('action');
        var action;
        if (actionAttr) {
            // action 已经是完整路径时直接使用，避免重复拼接 contextPath
            if (actionAttr.indexOf('/') === 0) {
                var ctx = window._pageConfig && window._pageConfig.contextPath || '';
                action = actionAttr.indexOf(ctx + '/') === 0 || actionAttr.indexOf(ctx) === 0
                    ? actionAttr
                    : ctx + actionAttr;
            } else {
                action = actionAttr;
            }
        } else {
            action = formEl.action;
        }
        var body;

        // 判断是否有文件上传
        var hasFile = formEl.querySelector('input[type="file"]');
        if (hasFile) {
            body = new FormData(formEl);
        } else {
            body = new URLSearchParams(new FormData(formEl)).toString();
        }

        var headers = {};
        var csrfToken = window._pageConfig && window._pageConfig.csrfToken;
        if (csrfToken) headers['X-CSRF-Token'] = csrfToken;
        headers['X-Requested-With'] = 'XMLHttpRequest';
        if (!hasFile) headers['Content-Type'] = 'application/x-www-form-urlencoded';

        var submitBtn = formEl.querySelector('button[type="submit"]');
        var origText = '';
        if (submitBtn) {
            origText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.textContent = '提交中...';
        }

        fetch(action, { method: method, headers: headers, body: body, credentials: 'same-origin' })
            .then(function(r) {
                var ct = r.headers.get('content-type') || '';
                var isJson = ct.indexOf('application/json') !== -1;

                // 非 2xx：尝试从 JSON body 读取后端错误消息
                if (!r.ok) {
                    if (isJson) {
                        return r.json().then(function(data) {
                            throw new Error(data.message || ('请求失败（' + r.status + '）'));
                        }).catch(function(e) {
                            if (e.message && e.message.indexOf('JSON') === -1 && e.message.indexOf('parse') === -1) {
                                throw e; // 重新抛出包含后端消息的错误
                            }
                            throw new Error('请求失败（' + r.status + '），请稍后重试');
                        });
                    }
                    if (r.status === 403) {
                        throw new Error('会话已过期或安全校验失败，请刷新页面后重试');
                    }
                    throw new Error('请求失败（' + r.status + '），请稍后重试');
                }

                // 2xx：如果返回重定向（非 JSON），跟随跳转
                if (!isJson) {
                    window.location.href = r.url;
                    return null;
                }
                return r.json();
            })
            .then(function(data) {
                if (!data) return;
                if (data.code === 200 || data.success) {
                    showToast(opts.successMsg || '操作成功', 'success');
                    if (opts.onSuccess) opts.onSuccess(data);
                    // 默认行为：1秒后刷新当前页
                    if (!opts.onSuccess) {
                        setTimeout(function() { location.reload(); }, 800);
                    }
                } else {
                    showToast(opts.errorMsg || data.message || '操作失败', 'error');
                    if (opts.onError) opts.onError(data);
                }
            })
            .catch(function(err) {
                showToast(err.message || '网络错误，请稍后重试', 'error');
                if (opts.onError) opts.onError(err);
            });
    };

    /**
     * 图片懒加载 — 使用 IntersectionObserver
     * 在列表页中，给 <img data-src="xxx"> 添加懒加载
     * @param {string} selector - 图片选择器，默认 'img[data-src]'
     */
    window.initLazyLoad = function(selector) {
        selector = selector || 'img[data-src]';
        var images = document.querySelectorAll(selector);
        if (!images.length) return;

        // 浏览器不支持 IO 时直接加载
        if (!('IntersectionObserver' in window)) {
            for (var i = 0; i < images.length; i++) {
                if (images[i].dataset.src) images[i].src = images[i].dataset.src;
            }
            return;
        }

        var observer = new IntersectionObserver(function(entries) {
            for (var j = 0; j < entries.length; j++) {
                if (entries[j].isIntersecting) {
                    var img = entries[j].target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                    }
                    observer.unobserve(img);
                }
            }
        }, { rootMargin: '100px' });

        for (var k = 0; k < images.length; k++) {
            observer.observe(images[k]);
        }
    };

    /**
     * 异步分页 — 点击分页链接时不刷新整页
     * @param {object} opts
     * @param {string} opts.containerSelector - 列表容器选择器 (如 '.card')
     * @param {string} opts.paginationSelector - 分页容器选择器 (如 '.pagination')
     * @param {function} opts.onLoaded - 内容加载后回调
     */
    window.initAjaxPagination = function(opts) {
        opts = opts || {};
        var containerSel = opts.containerSelector || '.card';
        var paginationSel = opts.paginationSelector || '.pagination';

        document.addEventListener('click', function(e) {
            var target = e.target;
            // 找到分页区域内的 <a>
            var pagArea = target.closest(paginationSel);
            if (!pagArea) return;
            var link = target.closest('a');
            if (!link || link.classList.contains('active') || link.classList.contains('disabled')) return;

            e.preventDefault();
            var url = link.href;
            if (!url) return;

            // 发起 AJAX 请求获取同一页
            fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' }, credentials: 'same-origin' })
                .then(function(r) { return r.text(); })
                .then(function(html) {
                    var parser = new DOMParser();
                    var doc = parser.parseFromString(html, 'text/html');

                    // 替换容器内容
                    var newContainer = doc.querySelector(containerSel);
                    var oldContainer = document.querySelector(containerSel);
                    if (newContainer && oldContainer) {
                        oldContainer.innerHTML = newContainer.innerHTML;
                    }

                    // 更新浏览器 URL（不刷新）
                    if (history.pushState) {
                        history.pushState(null, '', url);
                    }

                    // 重新初始化懒加载
                    initLazyLoad();

                    // 回调
                    if (opts.onLoaded) opts.onLoaded();

                    // 滚动到顶部
                    oldContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
                })
                .catch(function(err) {
                    showToast('加载失败', 'error');
                });
        });
    };

    /**
     * 实时搜索 — 输入防抖 + 异步刷新列表
     * @param {object} opts
     * @param {string} opts.inputSelector - 搜索输入框选择器
     * @param {string} opts.formSelector - 搜索表单选择器
     * @param {string} opts.containerSelector - 列表容器选择器
     * @param {number} opts.delay - 防抖延迟(毫秒)，默认 400
     * @param {function} opts.onLoaded - 加载完成回调
     */
    window.initLiveSearch = function(opts) {
        opts = opts || {};
        var inputSel = opts.inputSelector || '.search-input';
        var formSel = opts.formSelector || '.search-form';
        var containerSel = opts.containerSelector || '.card';
        var delay = opts.delay || 400;

        var input = document.querySelector(inputSel);
        var form = document.querySelector(formSel);
        if (!input || !form) return;

        var timer = null;

        input.addEventListener('input', function() {
            clearTimeout(timer);
            timer = setTimeout(function() {
                var formData = new FormData(form);
                var params = new URLSearchParams(formData).toString();
                var actionAttr = form.getAttribute('action') || '';
                var actionUrl = actionAttr.indexOf('/') === 0 ? (window._pageConfig && window._pageConfig.contextPath || '') + actionAttr : actionAttr || form.action;
                var url = actionUrl + (actionUrl.indexOf('?') > -1 ? '&' : '?') + params;

                fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' }, credentials: 'same-origin' })
                    .then(function(r) { return r.text(); })
                    .then(function(html) {
                        var parser = new DOMParser();
                        var doc = parser.parseFromString(html, 'text/html');
                        var newContainer = doc.querySelector(containerSel);
                        var oldContainer = document.querySelector(containerSel);
                        if (newContainer && oldContainer) {
                            oldContainer.innerHTML = newContainer.innerHTML;
                        }
                        // 重新初始化懒加载
                        initLazyLoad();
                        if (opts.onLoaded) opts.onLoaded();
                    })
                    .catch(function() {});
            }, delay);
        });
    };

    /**
     * 定时轮询 — 用于 Dashboard 实时刷新
     * @param {string} url - 请求 URL
     * @param {function} callback - 回调函数(data)
     * @param {number} interval - 间隔毫秒，默认 30000
     * @returns {number} intervalId — 可 clearInterval 停止
     */
    window.startPolling = function(url, callback, interval) {
        interval = interval || 30000;
        function poll() {
            fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' }, credentials: 'same-origin' })
                .then(function(r) {
                    var ct = r.headers.get('content-type') || '';
                    if (ct.indexOf('json') > -1) return r.json();
                    return r.text().then(function(html) { return { _html: html }; });
                })
                .then(function(data) {
                    if (callback) callback(data);
                })
                .catch(function() {});
        }
        poll(); // 立即执行首次轮询
        return setInterval(poll, interval);
    };

    // ==================== 汉堡菜单 ====================
    document.addEventListener('click', function(e) {
        var toggle = e.target.closest('#navbarToggle');
        if (toggle) {
            var menu = document.getElementById('navbarMenu');
            if (menu) {
                menu.classList.toggle('open');
                toggle.classList.toggle('open');
            }
            return;
        }
        // 点击菜单外区域关闭
        var openMenu = document.querySelector('.navbar-menu.open');
        if (openMenu && !e.target.closest('.navbar')) {
            openMenu.classList.remove('open');
            var t = document.getElementById('navbarToggle');
            if (t) t.classList.remove('open');
        }
    });

    // ==================== Lightbox 图片放大 ====================
    window.initLightbox = function() {
        var overlay = document.getElementById('lightboxOverlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'lightboxOverlay';
            overlay.className = 'lightbox-overlay';
            overlay.innerHTML = '<button class="lightbox-close">&times;</button><img src="" alt="放大预览">';
            document.body.appendChild(overlay);

            overlay.addEventListener('click', function(e) {
                if (e.target === overlay || e.target.classList.contains('lightbox-close')) {
                    overlay.classList.remove('show');
                }
            });

            document.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') overlay.classList.remove('show');
            });
        }

        // 给图片添加点击放大
        var images = document.querySelectorAll('.image-pair img, #imagePreview img');
        for (var i = 0; i < images.length; i++) {
            if (images[i].dataset.lightboxInit) continue;
            images[i].dataset.lightboxInit = '1';
            images[i].style.cursor = 'zoom-in';
            images[i].addEventListener('click', function() {
                var img = overlay.querySelector('img');
                img.src = this.src;
                overlay.classList.add('show');
            });
        }
    };

    // ==================== 数字递增动画 ====================
    window.animateCounter = function(el, target, duration) {
        if (!el || target === undefined) return;
        duration = duration || 800;
        var startTime = null;
        var targetStr = String(target);
        var suffix = targetStr.replace(/[\d.\-]/g, '');
        var targetNum = parseFloat(targetStr);

        function step(ts) {
            if (!startTime) startTime = ts;
            var progress = Math.min((ts - startTime) / duration, 1);
            var eased = 1 - Math.pow(1 - progress, 3);
            var current = targetNum * eased;

            if (Number.isInteger(targetNum)) {
                el.textContent = Math.round(current) + suffix;
            } else {
                el.textContent = current.toFixed(1) + suffix;
            }

            if (progress < 1) {
                requestAnimationFrame(step);
            }
        }
        requestAnimationFrame(step);
    };

    window.initCounterAnimation = function() {
        var cards = document.querySelectorAll('.stat-card .stat-value');
        for (var i = 0; i < cards.length; i++) {
            var text = cards[i].textContent.trim();
            if (/^\d+(\.\d+)?%?$/.test(text)) {
                animateCounter(cards[i], text);
            }
        }
    };

    // ==================== 分页增强 — 页码按钮 ====================
    window.initPaginationNumbers = function(opts) {
        opts = opts || {};
        var pageSel = opts.paginationSelector || '.pagination';
        var paginations = document.querySelectorAll(pageSel);

        for (var p = 0; p < paginations.length; p++) {
            var pag = paginations[p];
            if (pag.dataset.enhanced) continue;
            pag.dataset.enhanced = '1';

            var activeSpan = pag.querySelector('span.active');
            if (!activeSpan) continue;

            var parts = activeSpan.textContent.split('/');
            var currentPage = parseInt(parts[0], 10) || 1;
            var totalPages = parseInt(parts[1], 10) || 1;

            if (totalPages <= 1) continue;

            pag.innerHTML = '';

            // 上一页
            if (currentPage > 1) {
                var prevA = document.createElement('a');
                prevA.href = '?' + buildPageParam(currentPage - 1);
                prevA.textContent = '\u2039';
                prevA.className = 'page-num';
                pag.appendChild(prevA);
            }

            // 页码
            var startPage = Math.max(1, currentPage - 2);
            var endPage = Math.min(totalPages, currentPage + 2);

            if (startPage > 1) {
                pag.appendChild(createPageNum(1, currentPage));
                if (startPage > 2) {
                    var dots1 = document.createElement('span');
                    dots1.textContent = '...';
                    dots1.style.cssText = 'color:var(--text-muted);padding:0 4px;';
                    pag.appendChild(dots1);
                }
            }

            for (var i = startPage; i <= endPage; i++) {
                pag.appendChild(createPageNum(i, currentPage));
            }

            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    var dots2 = document.createElement('span');
                    dots2.textContent = '...';
                    dots2.style.cssText = 'color:var(--text-muted);padding:0 4px;';
                    pag.appendChild(dots2);
                }
                pag.appendChild(createPageNum(totalPages, currentPage));
            }

            // 下一页
            if (currentPage < totalPages) {
                var nextA = document.createElement('a');
                nextA.href = '?' + buildPageParam(currentPage + 1);
                nextA.textContent = '\u203A';
                nextA.className = 'page-num';
                pag.appendChild(nextA);
            }

            // 总条数信息
            var info = document.createElement('div');
            info.className = 'pagination-info';
            info.textContent = '\u7B2C ' + currentPage + ' / ' + totalPages + ' \u9875';
            pag.parentNode.insertBefore(info, pag.nextSibling);
        }
    };

    function createPageNum(page, current) {
        var el;
        if (page === current) {
            el = document.createElement('span');
            el.className = 'page-num active';
        } else {
            el = document.createElement('a');
            el.className = 'page-num';
            el.href = '?' + buildPageParam(page);
        }
        el.textContent = page;
        return el;
    }

    function buildPageParam(page) {
        var params = new URLSearchParams(window.location.search);
        params.set('page', page);
        return params.toString();
    }

    // ==================== 表格行展开详情 ====================
    window.initExpandableRows = function() {
        var rows = document.querySelectorAll('.table tr.expandable');
        for (var i = 0; i < rows.length; i++) {
            rows[i].addEventListener('click', function() {
                var nextRow = this.nextElementSibling;
                if (nextRow && nextRow.classList.contains('detail-row')) {
                    var content = nextRow.querySelector('.detail-content');
                    if (content) {
                        var isVisible = content.style.display !== 'none';
                        content.style.display = isVisible ? 'none' : 'block';
                        var toggle = this.querySelector('.expand-toggle');
                        if (toggle) toggle.classList.toggle('open', !isVisible);
                    }
                }
            });
        }
    };

    // ==================== 页面加载后初始化所有增强 ====================
    document.addEventListener('DOMContentLoaded', function() {
        initLightbox();
        initCounterAnimation();
        initPaginationNumbers();
        initExpandableRows();
    });

})();
