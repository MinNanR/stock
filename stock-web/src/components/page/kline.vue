<template>
  <div>
    <div style="display: flex; flex-direction: row; align-items: center">
      <el-button type="primary" style="font-size: 18px" @click="turnBack()"
        >后退</el-button
      >
      <div style="margin-left: 30px; font-size: 18px">
        {{ stockName }} : {{ stockCode }}
      </div>
    </div>
    <div id="charts" style="margin-top: 30px">
      <div id="myChart" style="width: 80vw; height: 600px"></div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      id: "",
      stockName: "",
      stockCode: "",
    };
  },
  methods: {
    drawChart(data) {
      // 基于准备好的dom，初始化echarts实例【这里存在一个问题，请看到最后】
      let chart = this.$echarts.init(document.getElementById("myChart"));
      // 指定图表的配置项和数据
      let option = {
        legend: {
          data: ["日K", "120日均线"],
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
          data: data.dates,
          axisLine: { lineStyle: { color: "#8392A5" } },
        },
        yAxis: {
          scale: true,
          axisLine: { lineStyle: { color: "#8392A5" } },
          splitLine: { show: false },
        },
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
          //   {
          //     type: "inside",
          //   },
        ],
        series: [
          {
            type: "candlestick",
            name: "日K",
            data: data.candlestickChartList,
            itemStyle: {
              color: "#FD1050",
              color0: "#0CF49B",
              borderColor: "#FD1050",
              borderColor0: "#0CF49B",
            },
          },
          {
            name: "120日均线",
            type: "line",
            data: data.avgLineData,
            smooth: true,
            showSymbol: false,
            lineStyle: {
              width: 1,
            },
          },
        ],
      };
      // 使用刚指定的配置项和数据显示图表。
      chart.setOption(option);
      document.getElementById("myChart").removeAttribute("_echarts_instance_");
    },
    getData() {
      this.request.post("/getKLineData", { id: this.id }).then((response) => {
        let data = response.data;
        this.drawChart(data);
      });
    },
    turnBack() {
      this.$router.go(-1);
    },
  },
  mounted() {
    let id = this.$route.params.id;
    let stockName = this.$route.params.stockName;
    let stockCode = this.$route.params.stockCode;
    this.id = id;
    this.stockName = stockName;
    this.stockCode = stockCode;
    this.getData();
  },
};
</script>

<style>
</style>