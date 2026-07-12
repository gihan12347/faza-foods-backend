(function () {
    'use strict';

    var body = document.body;
    var suggestedId = body.getAttribute('data-suggested-id') || '';
    var openModal = body.getAttribute('data-open-modal');
    var formModeBoot = body.getAttribute('data-form-mode') || 'create';

    var formModal = document.getElementById('rate-form-modal');
    var deleteModal = document.getElementById('rate-delete-modal');
    var rateForm = document.getElementById('rate-modal-form');
    var deleteForm = document.getElementById('rate-delete-form');
    var searchInput = document.getElementById('rate-search');
    var searchMeta = document.querySelector('.js-rate-search-meta');
    var visibleCountEl = document.querySelector('.js-rate-visible-count');

    var fields = {
        id: document.getElementById('sr-id'),
        idReadonly: document.getElementById('sr-id-readonly'),
        idCreateWrap: document.querySelector('.product-form__field--id-create'),
        idEditWrap: document.querySelector('.product-form__field--id-edit'),
        rateType: document.getElementById('sr-rate-type'),
        deliveryTypes: document.getElementById('sr-delivery-type'),
        minWeight: document.getElementById('sr-min-weight'),
        maxWeight: document.getElementById('sr-max-weight'),
        price: document.getElementById('sr-price')
    };

    var formTitle = document.getElementById('rate-form-modal-title');
    var formSubmitBtn = document.querySelector('.js-rate-form-submit');
    var deleteLabelEl = document.querySelector('.js-delete-rate-label');
    var deleteLabelInput = document.getElementById('delete-rate-label');
    var modalFormError = document.querySelector('.js-modal-form-error');

    function openModalEl(el) {
        if (!el) return;
        el.hidden = false;
        el.setAttribute('aria-hidden', 'false');
        document.documentElement.classList.add('dash-modal-open');
        var focusable = el.querySelector('input, button, textarea, select');
        if (focusable) {
            window.setTimeout(function () { focusable.focus(); }, 50);
        }
    }

    function closeModalEl(el) {
        if (!el) return;
        el.hidden = true;
        el.setAttribute('aria-hidden', 'true');
        if (!document.querySelector('.dash-modal:not([hidden])')) {
            document.documentElement.classList.remove('dash-modal-open');
        }
    }

    function closeModalByKey(key) {
        if (key === 'rate-form') closeModalEl(formModal);
        if (key === 'rate-delete') closeModalEl(deleteModal);
    }

    function buildUrl(path) {
        var base = document.querySelector('base');
        if (base && base.href) {
            try {
                return new URL(path.replace(/^\//, ''), base.href).pathname;
            } catch (e) { /* fall through */ }
        }
        return path;
    }

    function setFormModeCreate() {
        if (fields.idCreateWrap) fields.idCreateWrap.hidden = false;
        if (fields.idEditWrap) fields.idEditWrap.hidden = true;
        if (fields.id) {
            fields.id.disabled = false;
            fields.id.required = true;
        }
        if (formTitle) formTitle.textContent = 'Add shipping rate';
        if (formSubmitBtn) formSubmitBtn.textContent = 'Create rate';
        if (rateForm) rateForm.action = buildUrl('/shipping-rates');
    }

    function setFormModeEdit(id) {
        if (fields.idCreateWrap) fields.idCreateWrap.hidden = true;
        if (fields.idEditWrap) fields.idEditWrap.hidden = false;
        if (fields.id) {
            fields.id.disabled = true;
            fields.id.required = false;
        }
        if (fields.idReadonly) fields.idReadonly.value = String(id);
        if (formTitle) formTitle.textContent = 'Edit shipping rate #' + id;
        if (formSubmitBtn) formSubmitBtn.textContent = 'Save changes';
        if (rateForm) rateForm.action = buildUrl('/shipping-rates/' + id);
    }

    function fillForm(data) {
        if (!data) return;
        if (fields.id) fields.id.value = data.id != null ? data.id : '';
        if (fields.rateType) fields.rateType.value = data.rateType || 'normal';
        if (fields.deliveryTypes) fields.deliveryTypes.value = data.deliveryTypes || 'courier';
        if (fields.minWeight) fields.minWeight.value = data.minWeight != null ? data.minWeight : 0;
        if (fields.maxWeight) fields.maxWeight.value = data.maxWeight != null ? data.maxWeight : 0;
        if (fields.price) fields.price.value = data.price != null ? data.price : 0;
    }

    function resetFormForCreate() {
        fillForm({
            id: suggestedId,
            rateType: 'normal',
            deliveryTypes: 'courier',
            minWeight: 0,
            maxWeight: 0,
            price: 0
        });
        setFormModeCreate();
    }

    function hideModalFormError() {
        if (!modalFormError) return;
        modalFormError.hidden = true;
        modalFormError.textContent = '';
    }

    function openCreateModal() {
        hideModalFormError();
        resetFormForCreate();
        openModalEl(formModal);
    }

    function openEditModal(id) {
        hideModalFormError();
        setFormModeEdit(id);
        openModalEl(formModal);
        fetch(buildUrl('/shipping-rates/' + id + '/data'), {
            headers: { 'Accept': 'application/json' },
            credentials: 'same-origin'
        })
            .then(function (res) {
                if (!res.ok) throw new Error('Rate not found');
                return res.json();
            })
            .then(function (data) {
                fillForm(data);
                if (fields.id) fields.id.value = data.id;
            })
            .catch(function () {
                if (modalFormError) {
                    modalFormError.textContent = 'Could not load shipping rate. Please try again.';
                    modalFormError.hidden = false;
                }
            });
    }

    function openDeleteModal(id, label) {
        var text = label || ('#' + id);
        if (deleteLabelEl) deleteLabelEl.textContent = text;
        if (deleteLabelInput) deleteLabelInput.value = label || '';
        if (deleteForm) deleteForm.action = buildUrl('/shipping-rates/' + id + '/delete');
        openModalEl(deleteModal);
    }

    function filterRates() {
        var q = searchInput ? searchInput.value.trim().toLowerCase() : '';
        var items = document.querySelectorAll('.js-rate-item');
        var visibleIds = {};
        items.forEach(function (row) {
            var hay = (row.getAttribute('data-search') || '').toLowerCase();
            var show = !q || hay.indexOf(q) !== -1;
            row.hidden = !show;
            if (show) {
                var rid = row.getAttribute('data-rate-id');
                if (rid) visibleIds[rid] = true;
            }
        });
        if (searchMeta && visibleCountEl) {
            searchMeta.hidden = items.length === 0;
            visibleCountEl.textContent = String(Object.keys(visibleIds).length);
        }
    }

    document.querySelectorAll('.js-rate-add').forEach(function (btn) {
        btn.addEventListener('click', openCreateModal);
    });

    document.querySelectorAll('.js-rate-edit').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var id = btn.getAttribute('data-id');
            if (id) openEditModal(id);
        });
    });

    document.querySelectorAll('.js-rate-delete').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var id = btn.getAttribute('data-id');
            var label = btn.getAttribute('data-label');
            if (id) openDeleteModal(id, label);
        });
    });

    document.querySelectorAll('.js-modal-close').forEach(function (el) {
        el.addEventListener('click', function () {
            closeModalByKey(el.getAttribute('data-modal'));
        });
    });

    document.addEventListener('keydown', function (ev) {
        if (ev.key === 'Escape') {
            closeModalEl(formModal);
            closeModalEl(deleteModal);
        }
    });

    if (searchInput) {
        searchInput.addEventListener('input', filterRates);
        filterRates();
    }

    if (openModal === 'form') {
        if (formModeBoot === 'edit' && fields.id && fields.id.value) {
            setFormModeEdit(fields.id.value);
            openModalEl(formModal);
        } else {
            setFormModeCreate();
            openModalEl(formModal);
        }
        if (modalFormError && body.querySelector('.flash--err')) {
            var pageErr = body.querySelector('.panel .flash--err');
            if (pageErr && pageErr.textContent) {
                modalFormError.textContent = pageErr.textContent.trim();
                modalFormError.hidden = false;
            }
        }
    }
})();
