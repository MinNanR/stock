<template>
  <div>
    <div id="charts" style="margin-top: 30px">
      <div id="myChart" style="width: 80vw; height: 600px"></div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      smooth: true,
    };
  },
  methods: {
    getData() {
      this.request.post("/statistics/getMarketLimitLine").then((response) => {
        let data = response.data;
        this.drawChart(data);
      });
    },
    drawChart(data) {
      // 基于准备好的dom，初始化echarts实例【这里存在一个问题，请看到最后】
      let chart = this.$echarts.init(document.getElementById("myChart"));
      // 指定图表的配置项和数据
      let option = {
        legend: {
          data: ["涨停数量", "跌停数量", "差值", "上证指数","上证50", "中证500", "沪深300"],
          inactiveColor: "#777",
        },
        tooltip: {
          trigger: "axis",
          axisPointer: {
            animation: false,
            type: "cross",
            lineStyle: {
              color: "#376df4",
              width: 2,
              opacity: 1,
            },
          },
        },
        xAxis: {
          type: "category",
          data: data.noteDate,
          axisLine: { lineStyle: { color: "#8392A5" } },
        },
        yAxis: [
          {
            scale: true,
            axisLine: { lineStyle: { color: "#8392A5" } },
            splitLine: { show: false },
          },
          {
            scale: true,
            axisLine: { lineStyle: { color: "#1F7EFF" } },
            splitLine: { show: false },
          },
        ],
        grid: {
          bottom: 80,
        },
        dataZoom: [
          {
            type: "inside",
            start: 90,
            end: 100,
          },
          {
            textStyle: {
              color: "#8392A5",
            },
            handleIcon:
              "path://M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z",
            dataBackground: {
              areaStyle: {
                color: "#8392A5",
              },
              lineStyle: {
                opacity: 0.8,
                color: "#8392A5",
              },
            },
            brushSelect: true,
            start: 90,
            end: 100,
          },
        ],
        series: [
          {
            type: "line",
            name: "涨停数量",
            data: data.surgedLineData,
            smooth: this.smooth,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
            yAxisIndex: 0,
          },
          {
            type: "line",
            name: "跌停数量",
            data: data.declineLineData,
            smooth: this.smooth,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
            yAxisIndex: 0,
          },
          {
            type: "line",
            name: "差值",
            data: data.differ,
            smooth: this.smooth,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
            yAxisIndex: 0,
          },
          {
            type: "line",
            name: "上证指数",
            data: data.sh000001,
            smooth: this.smooth,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
            yAxisIndex: 1,
          },
          {
            type: "line",
            name: "上证50",
            data: data.sh000016,
            smooth: this.smooth,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
            yAxisIndex: 1,
          },
          {
            type: "line",
            name: "中证500",
            data: data.sh000905,
            smooth: this.smooth,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
            yAxisIndex: 1,
          },
          {
            type: "line",
            name: "沪深300",
            data: data.sz399300,
            smooth: this.smooth,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
            yAxisIndex: 1,
          },
        ],
        // color: ["#c87183", "#8ae7e3", "#0000ff", "#D2691E"],
      };
      // 使用刚指定的配置项和数据显示图表。
      chart.setOption(option);
      document.getElementById("myChart").removeAttribute("_echarts_instance_");
    },
  },
  mounted() {
    this.getData();
  },
};
</script>

<style>
</style>