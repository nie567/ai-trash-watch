/**
 * 统计分析页 - ECharts 图表
 * 依赖: echarts
 */
(function() {
    'use strict';

    var config = window._pageConfig;

    // ==================== ECharts 暗色主题基础配置 ====================
    var darkAxis = {
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.12)' } },
        axisTick: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
        axisLabel: { color: '#94A3B8' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
    };

    // ==================== ECharts 图表（带库文件加载失败降级）====================

    var pieChart, barChart, violationTypeChart, violationLevelChart;

    function initCharts() {
        if (typeof echarts === 'undefined') {
            // 库文件加载失败，显示静态文字降级
            ['pieChart', 'barChart', 'violationTypeChart', 'violationLevelChart'].forEach(function(id) {
                var el = document.getElementById(id);
                if (el) {
                    el.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--text-secondary,#94A3B8);font-size:14px;">图表库加载失败，请检查网络连接</div>';
                }
            });
            return;
        }

        // 饼图 - 垃圾类别分布（配色与分类语义对应）
        pieChart = echarts.init(document.getElementById('pieChart'));
        pieChart.setOption({
            tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
            legend: { bottom: 10, left: 'center', textStyle: { color: '#94A3B8' } },
            color: ['#3B82F6', '#0EA5E9', '#8B5CF6', '#F59E0B'],
            series: [{
                name: '垃圾类别',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 10, borderColor: 'rgba(255,255,255,0.1)', borderWidth: 2 },
                label: { show: false, position: 'center' },
                emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold', color: '#F1F5F9' } },
                labelLine: { show: false },
                data: config.pieData
            }]
        });

        // 柱状图 - 正确/错误对比
        barChart = echarts.init(document.getElementById('barChart'));
        barChart.setOption({
            tooltip: { trigger: 'axis' },
            xAxis: Object.assign({ type: 'category', data: ['正确投放', '错误投放'] }, darkAxis),
            yAxis: Object.assign({ type: 'value' }, darkAxis),
            series: [{
                type: 'bar',
                data: [
                    { value: config.correctCount, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#38BDF8' }, { offset: 1, color: '#0EA5E9' }]) } },
                    { value: config.wrongCount, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#FB923C' }, { offset: 1, color: '#F97316' }]) } }
                ],
                barWidth: '40%',
                itemStyle: { borderRadius: [6, 6, 0, 0] }
            }]
        });

        // 饼图 - 违规类型分布
        violationTypeChart = echarts.init(document.getElementById('violationTypeChart'));
        violationTypeChart.setOption({
            tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
            legend: { bottom: 10, left: 'center', textStyle: { color: '#94A3B8' } },
            color: ['#3B82F6', '#60A5FA', '#0EA5E9', '#F59E0B', '#8B5CF6'],
            series: [{
                name: '违规类型',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 10, borderColor: 'rgba(255,255,255,0.1)', borderWidth: 2 },
                label: { show: false, position: 'center' },
                emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold', color: '#F1F5F9' } },
                labelLine: { show: false },
                data: config.violationTypeData
            }]
        });

        // 饼图 - 违规等级分布
        violationLevelChart = echarts.init(document.getElementById('violationLevelChart'));
        violationLevelChart.setOption({
            tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
            legend: { bottom: 10, left: 'center', textStyle: { color: '#94A3B8' } },
            color: ['#EF4444', '#F97316', '#EAB308'],
            series: [{
                name: '违规等级',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 10, borderColor: 'rgba(255,255,255,0.1)', borderWidth: 2 },
                label: { show: false, position: 'center' },
                emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold', color: '#F1F5F9' } },
                labelLine: { show: false },
                data: config.violationLevelData
            }]
        });

        // 窗口 resize 自适应（仅图表初始化成功后才绑定）
        window.addEventListener('resize', function() {
            if (pieChart) pieChart.resize();
            if (barChart) barChart.resize();
            if (violationTypeChart) violationTypeChart.resize();
            if (violationLevelChart) violationLevelChart.resize();
        });
    }

    initCharts();
})();
