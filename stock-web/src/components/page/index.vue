<template>
  <div>
    <el-form :inline="true" :model="queryForm" class="demo-form-inline">
      <el-form-item label="统计日期">
        <el-date-picker
          v-model="queryForm.noteDate"
          type="date"
          placeholder="选择统计日期"
          value-format="YYYY-MM-DD"
        >
        </el-date-picker>
      </el-form-item>
      <!-- <el-form-item label="代码/名称">
        <el-input v-model="queryForm.keyword" placeholder="输入股票代码或名称"></el-input>
      </el-form-item>-->
      <el-form-item>
        <el-button type="primary" @click="querySotckList">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" stripe style="width: 100%">
      <el-table-column type="index" width="50" />
      <el-table-column prop="stockName" label="名称" width="150" />
      <el-table-column prop="stockCode" label="代码" width="150" />
      <el-table-column prop="startPrice" label="当日开盘价" width="150" />
      <el-table-column prop="endPrice" label="当日收盘价" width="150" />
      <el-table-column prop="highestPrice" label="当日最高价" width="150" />
      <el-table-column prop="lowestPrice" label="当日最低价" width="150" />
      <el-table-column
        prop="avgPricePast120Days"
        label="120日均价"
        width="150"
      />
      <el-table-column prop="createTime" label="记录时间" width="150" />
      <el-table-column label="操作">
        <el-button type="primary">查看</el-button>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import dayjs from "dayjs";

export default {
  data() {
    return {
      tableData: [
        {
          stockName: "龙建股份",
          stockCode: "600853",
          endPrice: "2.4900",
        },
        {
          stockName: "天邦股份",
          stockCode: "002124",
          endPrice: "6.4100",
        },
      ],
      queryForm: {
        // keyword: "",
        noteDate: "",
        pageIndex: 1,
        pageSize: 10,
      },
    };
  },
  methods: {
    querySotckList() {
      this.request
        .post("/stock/getEligibleStockList", this.queryForm)
        .then((response) => {
          let data = response.data
          this.tableData = data["stockList"]
        });
    },
  },
  mounted() {
    this.queryForm.noteDate = dayjs().format("YYYY-MM-DD");
  },
};
</script>

<style>
</style>