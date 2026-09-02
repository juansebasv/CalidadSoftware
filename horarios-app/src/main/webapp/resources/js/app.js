/* Realces de interfaz del lado del cliente (sin dependencias externas). */
(function () {
    "use strict";

    /** Efecto "ripple" manual para elementos marcados con .js-ripple. */
    function enlazarRipple() {
        document.querySelectorAll(".js-ripple").forEach(function (el) {
            if (el.dataset.rippleBound) { return; }
            el.dataset.rippleBound = "1";
            el.addEventListener("click", function (e) {
                var circulo = document.createElement("span");
                var d = Math.max(el.clientWidth, el.clientHeight);
                circulo.style.cssText =
                    "position:absolute;border-radius:50%;transform:scale(0);opacity:.5;" +
                    "background:rgba(255,255,255,.7);pointer-events:none;width:" + d + "px;height:" + d + "px;" +
                    "left:" + (e.offsetX - d / 2) + "px;top:" + (e.offsetY - d / 2) + "px;" +
                    "transition:transform .5s ease,opacity .8s ease";
                el.style.position = "relative";
                el.style.overflow = "hidden";
                el.appendChild(circulo);
                requestAnimationFrame(function () {
                    circulo.style.transform = "scale(2.2)";
                    circulo.style.opacity = "0";
                });
                setTimeout(function () { circulo.remove(); }, 800);
            });
        });
    }

    /** Anima con un conteo ascendente los valores .js-contador. */
    function animarContadores() {
        document.querySelectorAll(".js-contador").forEach(function (el) {
            if (el.dataset.contado) { return; }
            el.dataset.contado = "1";
            var destino = parseInt(el.textContent.replace(/\D/g, ""), 10) || 0;
            var inicio = 0;
            var paso = Math.max(1, Math.round(destino / 40));
            var timer = setInterval(function () {
                inicio += paso;
                if (inicio >= destino) { inicio = destino; clearInterval(timer); }
                el.textContent = inicio;
            }, 18);
        });
    }

    /** Extender de PrimeFaces charts: respeta la altura del contenedor. */
    window.graficaMini = function () {
        this.cfg = this.cfg || {};
        this.cfg.config = this.cfg.config || {};
        this.cfg.config.options = this.cfg.config.options || {};
        this.cfg.config.options.maintainAspectRatio = false;
        this.cfg.config.options.responsive = true;
    };

    function init() {
        enlazarRipple();
        animarContadores();
    }

    document.addEventListener("DOMContentLoaded", init);
    // Re-enlazar tras cada actualizacion AJAX de JSF/PrimeFaces.
    if (window.PrimeFaces) {
        document.addEventListener("pfAjaxComplete", init);
    }
})();
