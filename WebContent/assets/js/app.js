/* 墨卷书阁 · 轻量交互 */
(function () {
    'use strict';

    /* 删除等危险操作二次确认 */
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            var message = form.getAttribute('data-confirm') || '确定执行这个操作吗？';
            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    });

    /* 密码显示 / 隐藏 */
    document.querySelectorAll('[data-toggle-password]').forEach(function (button) {
        button.addEventListener('click', function () {
            var input = document.getElementById(button.getAttribute('data-toggle-password'));
            if (!input) {
                return;
            }
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            button.textContent = show ? '隐藏' : '显示';
        });
    });

    /* 下拉排序自动提交 */
    document.querySelectorAll('select[data-autosubmit]').forEach(function (select) {
        select.addEventListener('change', function () {
            if (select.form) {
                select.form.submit();
            }
        });
    });

    /* 板块图标快捷选择 */
    document.querySelectorAll('[data-emoji]').forEach(function (button) {
        button.addEventListener('click', function () {
            var input = document.getElementById('icon');
            if (input) {
                input.value = button.getAttribute('data-emoji');
                input.focus();
            }
        });
    });

    /* 提示条自动消失 */
    var flash = document.querySelector('.flash');
    if (flash) {
        var close = flash.querySelector('.flash-close');
        var hide = function () {
            flash.classList.add('hide');
            window.setTimeout(function () {
                var wrap = flash.closest('.flash-wrap');
                if (wrap) {
                    wrap.remove();
                }
            }, 320);
        };
        if (close) {
            close.addEventListener('click', hide);
        }
        window.setTimeout(hide, 4200);
    }
})();
