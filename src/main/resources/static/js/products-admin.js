(function () {
    'use strict';

    var body = document.body;
    var suggestedId = body.getAttribute('data-suggested-id') || '';
    var openModal = body.getAttribute('data-open-modal');
    var formModeBoot = body.getAttribute('data-form-mode') || 'create';

    var formModal = document.getElementById('product-form-modal');
    var deleteModal = document.getElementById('product-delete-modal');
    var productForm = document.getElementById('product-modal-form');
    var deleteForm = document.getElementById('product-delete-form');
    var searchInput = document.getElementById('product-search');
    var searchMeta = document.querySelector('.js-product-search-meta');
    var visibleCountEl = document.querySelector('.js-product-visible-count');

    var fields = {
        id: document.getElementById('pf-id'),
        idReadonly: document.getElementById('pf-id-readonly'),
        idCreateWrap: document.querySelector('.product-form__field--id-create'),
        idEditWrap: document.querySelector('.product-form__field--id-edit'),
        name: document.getElementById('pf-name'),
        category: document.getElementById('pf-category'),
        description: document.getElementById('pf-description'),
        bestSeller: document.getElementById('pf-best-seller'),
        deliveryFree: document.getElementById('pf-delivery-free'),
        price: document.getElementById('pf-price'),
        originalPrice: document.getElementById('pf-original-price'),
        currentStock: document.getElementById('pf-current-stock'),
        minimumStock: document.getElementById('pf-minimum-stock'),
        weight: document.getElementById('pf-weight'),
        image: document.getElementById('pf-image'),
        imagesText: document.getElementById('pf-images'),
        howToUse: document.getElementById('pf-how-to-use'),
        ingredientsText: document.getElementById('pf-ingredients'),
        useForText: document.getElementById('pf-use-for')
    };

    var formTitle = document.getElementById('product-form-modal-title');
    var formSubmitBtn = document.querySelector('.js-product-form-submit');
    var deleteNameEl = document.querySelector('.js-delete-product-name');
    var deleteNameInput = document.getElementById('delete-product-name');
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
        if (key === 'product-form') closeModalEl(formModal);
        if (key === 'product-delete') closeModalEl(deleteModal);
    }

    function setFormModeCreate() {
        if (fields.idCreateWrap) fields.idCreateWrap.hidden = false;
        if (fields.idEditWrap) fields.idEditWrap.hidden = true;
        if (fields.id) {
            fields.id.disabled = false;
            fields.id.required = true;
        }
        if (formTitle) formTitle.textContent = 'Add product';
        if (formSubmitBtn) formSubmitBtn.textContent = 'Create product';
        if (productForm) productForm.action = buildUrl('/products');
    }

    function setFormModeEdit(id) {
        if (fields.idCreateWrap) fields.idCreateWrap.hidden = true;
        if (fields.idEditWrap) fields.idEditWrap.hidden = false;
        if (fields.id) {
            fields.id.disabled = true;
            fields.id.required = false;
        }
        if (fields.idReadonly) fields.idReadonly.value = String(id);
        if (formTitle) formTitle.textContent = 'Edit product #' + id;
        if (formSubmitBtn) formSubmitBtn.textContent = 'Save changes';
        if (productForm) productForm.action = buildUrl('/products/' + id);
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

    function fillForm(data) {
        if (!data) return;
        if (fields.id) fields.id.value = data.id != null ? data.id : '';
        if (fields.name) fields.name.value = data.name || '';
        if (fields.category) fields.category.value = data.category || '';
        if (fields.description) fields.description.value = data.description || '';
        if (fields.bestSeller) fields.bestSeller.checked = !!data.bestSeller;
        if (fields.deliveryFree) fields.deliveryFree.checked = !!data.deliveryFree;
        if (fields.price) fields.price.value = data.price != null ? data.price : 0;
        if (fields.originalPrice) fields.originalPrice.value = data.originalPrice != null ? data.originalPrice : 0;
        if (fields.currentStock) fields.currentStock.value = data.currentStock != null ? data.currentStock : 0;
        if (fields.minimumStock) fields.minimumStock.value = data.minimumStock != null ? data.minimumStock : 0;
        if (fields.weight) fields.weight.value = data.weight || '';
        if (fields.image) fields.image.value = data.image || '';
        if (fields.imagesText) fields.imagesText.value = data.imagesText || '';
        if (fields.howToUse) fields.howToUse.value = data.howToUse || '';
        if (fields.ingredientsText) fields.ingredientsText.value = data.ingredientsText || '';
        if (fields.useForText) fields.useForText.value = data.useForText || '';
    }

    function resetFormForCreate() {
        fillForm({
            id: suggestedId,
            name: '',
            description: '',
            price: 0,
            originalPrice: 0,
            bestSeller: false,
            deliveryFree: false,
            weight: '',
            image: '',
            howToUse: '',
            category: '',
            currentStock: 0,
            minimumStock: 0,
            ingredientsText: '',
            useForText: '',
            imagesText: ''
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
        fetch(buildUrl('/products/' + id + '/data'), {
            headers: { 'Accept': 'application/json' },
            credentials: 'same-origin'
        })
            .then(function (res) {
                if (!res.ok) throw new Error('Product not found');
                return res.json();
            })
            .then(function (data) {
                fillForm(data);
                if (fields.id) fields.id.value = data.id;
            })
            .catch(function () {
                if (modalFormError) {
                    modalFormError.textContent = 'Could not load product. Please try again.';
                    modalFormError.hidden = false;
                }
            });
    }

    function openDeleteModal(id, name) {
        var label = name || ('ID ' + id);
        if (deleteNameEl) deleteNameEl.textContent = label;
        if (deleteNameInput) deleteNameInput.value = name || '';
        if (deleteForm) deleteForm.action = buildUrl('/products/' + id + '/delete');
        openModalEl(deleteModal);
    }

    function filterProducts() {
        var q = searchInput ? searchInput.value.trim().toLowerCase() : '';
        var items = document.querySelectorAll('.js-product-item');
        var visibleIds = {};
        items.forEach(function (row) {
            var hay = (row.getAttribute('data-search') || '').toLowerCase();
            var show = !q || hay.indexOf(q) !== -1;
            row.hidden = !show;
            if (show) {
                var pid = row.getAttribute('data-product-id');
                if (pid) visibleIds[pid] = true;
            }
        });
        if (searchMeta && visibleCountEl) {
            searchMeta.hidden = items.length === 0;
            visibleCountEl.textContent = String(Object.keys(visibleIds).length);
        }
    }

    document.querySelectorAll('.js-product-add').forEach(function (btn) {
        btn.addEventListener('click', openCreateModal);
    });

    document.querySelectorAll('.js-product-edit').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var id = btn.getAttribute('data-id');
            if (id) openEditModal(id);
        });
    });

    document.querySelectorAll('.js-product-delete').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var id = btn.getAttribute('data-id');
            var name = btn.getAttribute('data-name');
            if (id) openDeleteModal(id, name);
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
        searchInput.addEventListener('input', filterProducts);
        filterProducts();
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
