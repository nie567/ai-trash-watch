/**
 * 管理员仪表盘
 * 增强: ECharts 图表 + 实时数据轮询
 * 依赖: echarts, dayjs, common.js
 */
(function() {
    'use strict';

    var config = window._pageConfig;
    var contextPath = config.contextPath;

    // ==================== ECharts 暗色主题基础配置 ====================
    var darkAxis = {
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.12)' } },
        axisTick: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
        axisLabel: { color: '#94A3B8' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
    };

    // ==================== ECharts 图表（带库文件加载失败降级）====================

    var pieChart, trendChart;

    function initCharts() {
        if (typeof echarts === 'undefined') {
            // 库文件加载失败，显示静态文字降级
            ['pieChart', 'trendChart'].forEach(function(id) {
                var el = document.getElementById(id);
                if (el) {
                    el.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--text-secondary,#94A3B8);font-size:14px;">图表库加载失败，请检查网络连接</div>';
                }
            });
            return;
        }

        // 饼图 - 正确率（环形图，中间显示百分比）
        var correctRate = config.totalRecords > 0
            ? (config.correctCount * 100.0 / config.totalRecords).toFixed(1)
            : '0.0';
        pieChart = echarts.init(document.getElementById('pieChart'));
        pieChart.setOption({
            tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
            legend: { bottom: 10, left: 'center', textStyle: { color: '#94A3B8' } },
            series: [{
                name: '投放结果',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 10, borderColor: 'rgba(255,255,255,0.1)', borderWidth: 2 },
                label: {
                    show: true,
                    position: 'center',
                    formatter: function() { return correctRate + '%'; },
                    fontSize: 28,
                    fontWeight: 'bold',
                    color: '#F1F5F9'
                },
                emphasis: { label: { show: true, fontSize: 28, fontWeight: 'bold', color: '#F1F5F9' } },
                labelLine: { show: false },
                data: [
                    { value: config.correctCount, name: '正确投放', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#38BDF8' }, { offset: 1, color: '#0EA5E9' }]) } },
                    { value: config.wrongCount, name: '错误投放', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#FB923C' }, { offset: 1, color: '#F97316' }]) } }
                ]
            }]
        });

        // 折线图 - 近7日趋势
        trendChart = echarts.init(document.getElementById('trendChart'));
        // 生成近7日日期标签
        var trendDates = [];
        for (var i = 6; i >= 0; i--) {
            trendDates.push(dayjs().subtract(i, 'day').format('MM-DD'));
        }
        trendChart.setOption({
            tooltip: { trigger: 'axis' },
            xAxis: Object.assign({ type: 'category', data: trendDates }, darkAxis),
            yAxis: Object.assign({ type: 'value', minInterval: 1 }, darkAxis),
            grid: { left: 40, right: 20, top: 20, bottom: 30 },
            series: [{
                name: '投放数',
                type: 'line',
                data: (config.trendData || [0,0,0,0,0,0,0]),
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { color: '#3B82F6', width: 3, shadowColor: 'rgba(59,130,246,0.5)', shadowBlur: 10 },
                itemStyle: { color: '#3B82F6', borderColor: '#fff', borderWidth: 1 },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(59,130,246,0.25)' },
                        { offset: 1, color: 'rgba(59,130,246,0)' }
                    ])
                }
            }]
        });
    }

    initCharts();

    // ==================== 实时刷新 ====================

    function updateStats(data) {
        if (!data) return;

        // 更新统计卡片
        if (data.totalUsers !== undefined) {
            var el1 = document.getElementById('statTotalUsers');
            if (el1) el1.textContent = data.totalUsers;
        }
        if (data.todayNew !== undefined) {
            var el2 = document.getElementById('statTodayNew');
            if (el2) el2.textContent = data.todayNew;
        }
        if (data.correctCount !== undefined) {
            var el3 = document.getElementById('statCorrectCount');
            if (el3) el3.textContent = data.correctCount;
        }
        if (data.wrongCount !== undefined) {
            var el4 = document.getElementById('statWrongCount');
            if (el4) el4.textContent = data.wrongCount;
        }
        if (data.totalRecords !== undefined) {
            var el5 = document.getElementById('statTotalRecords');
            if (el5) el5.textContent = data.totalRecords;
        }

        // 更新饼图
        if ((data.correctCount !== undefined || data.wrongCount !== undefined) && pieChart) {
            pieChart.setOption({
                series: [{
                    data: [
                        { value: data.correctCount || 0, name: '正确投放', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#38BDF8' }, { offset: 1, color: '#0EA5E9' }]) } },
                        { value: data.wrongCount || 0, name: '错误投放', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#FB923C' }, { offset: 1, color: '#F97316' }]) } }
                    ]
                }]
            });
        }

        // 更新趋势图
        if (data.trendData && trendChart) {
            trendChart.setOption({
                series: [{ data: data.trendData }]
            });
        }
    }

    // 每 30 秒轮询一次最新数据，页面离开时自动停止
    var pollId = startPolling(contextPath + '/admin/dashboard/api', function(data) {
        if (data && data.code === 200) {
            updateStats(data.data);
        } else if (data && !data.code) {
            updateStats(data);
        }
    }, 30000);

    window.addEventListener('beforeunload', function() {
        clearInterval(pollId);
    });
})();
