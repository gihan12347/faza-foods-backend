(function () {
    'use strict';

    var data = window.FASA_ANALYTICS_CHARTS;
    if (!data || typeof Chart === 'undefined') {
        return;
    }

    var colors = {
        primary: '#1e4a24',
        primaryMid: '#3d6b32',
        accent: '#7ec850',
        accentSoft: '#e8f5ea',
        accentMid: '#5fa83e',
        muted: '#94a89f',
        border: '#d4e8d6'
    };

    var palette = [
        colors.primary,
        colors.primaryMid,
        colors.accentMid,
        colors.accent,
        '#4a8f3a',
        '#2d5c28',
        '#a8d878',
        '#6b9e4f'
    ];

    var statusPalette = {
        Pending: '#f59e0b',
        Processing: '#3b82f6',
        Delivered: colors.accentMid,
        Done: colors.primary,
        Reject: '#ef4444'
    };

    function formatRs(value) {
        var n = Number(value) || 0;
        return 'Rs. ' + n.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
    }

    function baseChartOptions() {
        return {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        font: { family: 'Poppins, sans-serif', size: 11 },
                        color: '#5c6f6a'
                    }
                },
                tooltip: {
                    titleFont: { family: 'Poppins, sans-serif' },
                    bodyFont: { family: 'Poppins, sans-serif' }
                }
            },
            scales: {}
        };
    }

    function shortenLabels(labels, max) {
        return (labels || []).map(function (label) {
            var text = String(label || '');
            if (text.length <= max) {
                return text;
            }
            return text.slice(0, max - 3) + '...';
        });
    }

    function createLineChart() {
        var canvas = document.getElementById('chart-sales-trend');
        if (!canvas) return;

        var opts = baseChartOptions();
        opts.interaction = { mode: 'index', intersect: false };
        opts.scales = {
            x: {
                grid: { color: 'rgba(212, 232, 214, 0.6)' },
                ticks: { color: '#5c6f6a', maxRotation: 45, autoSkip: true, maxTicksLimit: 12 }
            },
            y: {
                position: 'left',
                grid: { color: 'rgba(212, 232, 214, 0.6)' },
                ticks: {
                    color: '#5c6f6a',
                    callback: function (v) { return formatRs(v); }
                }
            },
            y1: {
                position: 'right',
                grid: { drawOnChartArea: false },
                ticks: { color: '#5c6f6a', precision: 0 }
            }
        };

        new Chart(canvas, {
            type: 'line',
            data: {
                labels: data.dailyLabels || [],
                datasets: [
                    {
                        label: 'Revenue',
                        data: data.dailyRevenue || [],
                        borderColor: colors.primaryMid,
                        backgroundColor: 'rgba(126, 200, 80, 0.15)',
                        fill: true,
                        tension: 0.35,
                        yAxisID: 'y',
                        pointRadius: 2,
                        pointHoverRadius: 4
                    },
                    {
                        label: 'Orders',
                        data: data.dailyOrders || [],
                        borderColor: colors.accentMid,
                        backgroundColor: 'transparent',
                        borderDash: [4, 4],
                        tension: 0.35,
                        yAxisID: 'y1',
                        pointRadius: 2
                    }
                ]
            },
            options: opts
        });
    }

    function createBarChart(canvasId, labels, values, label, horizontal) {
        var canvas = document.getElementById(canvasId);
        if (!canvas) return;

        var opts = baseChartOptions();
        var axisKey = horizontal ? 'y' : 'x';
        var valueKey = horizontal ? 'x' : 'y';
        opts.indexAxis = horizontal ? 'y' : 'x';
        opts.scales = {};
        opts.scales[axisKey] = {
            grid: { display: false },
            ticks: { color: '#5c6f6a' }
        };
        opts.scales[valueKey] = {
            grid: { color: 'rgba(212, 232, 214, 0.6)' },
            ticks: {
                color: '#5c6f6a',
                callback: label === 'Revenue' ? function (v) { return formatRs(v); } : undefined
            }
        };
        opts.plugins.legend = { display: false };

        new Chart(canvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: values,
                    backgroundColor: palette.map(function (c, i) {
                        return i === 0 ? colors.primaryMid : c;
                    }),
                    borderRadius: 8,
                    maxBarThickness: horizontal ? 22 : 36
                }]
            },
            options: opts
        });
    }

    function createDoughnutChart(canvasId, labels, values, colorMap) {
        var canvas = document.getElementById(canvasId);
        if (!canvas) return;

        var bg = (labels || []).map(function (label, i) {
            if (colorMap && colorMap[label]) {
                return colorMap[label];
            }
            return palette[i % palette.length];
        });

        var opts = baseChartOptions();
        opts.cutout = '62%';
        opts.plugins.legend = {
            position: 'bottom',
            labels: {
                font: { family: 'Poppins, sans-serif', size: 11 },
                color: '#5c6f6a',
                boxWidth: 12
            }
        };

        new Chart(canvas, {
            type: 'doughnut',
            data: {
                labels: labels || [],
                datasets: [{
                    data: values || [],
                    backgroundColor: bg,
                    borderColor: '#ffffff',
                    borderWidth: 2
                }]
            },
            options: opts
        });
    }

    createLineChart();

    createDoughnutChart(
        'chart-revenue-split',
        ['Products', 'Delivery fees'],
        data.revenueSplit || [0, 0]
    );

    createDoughnutChart(
        'chart-order-status',
        data.statusLabels || [],
        data.statusCounts || [],
        statusPalette
    );

    createBarChart(
        'chart-top-products',
        shortenLabels(data.topProductLabels, 24),
        data.topProductQty || [],
        'Units sold',
        true
    );

    createBarChart(
        'chart-low-products',
        shortenLabels(data.lowProductLabels, 24),
        data.lowProductQty || [],
        'Units sold',
        true
    );

    createBarChart(
        'chart-districts',
        shortenLabels(data.districtLabels, 16),
        data.districtRevenue || [],
        'Revenue',
        false
    );

    createDoughnutChart(
        'chart-delivery-type',
        data.deliveryLabels || [],
        data.deliveryCounts || []
    );
})();
