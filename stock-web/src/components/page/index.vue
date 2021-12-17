<template>
  <div style="margin-bottom: 30px">
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
        <template #default="scope">
          <el-button type="primary" @click="refer(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display: flex; margin-top: 30px">
      <!-- <div class="refresh-btn" @click="getTenantList()">
        <i class="el-icon-refresh-right"></i>
      </div> -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="queryForm.pageIndex"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="10"
        :hide-on-single-page="false"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import dayjs from "dayjs";

export default {
  name:"index",
  data() {
    return {
      tableData: [],
      queryForm: {
        // keyword: "",
        noteDate: "",
        pageIndex: 1,
        pageSize: 10,
      },
      total: 0,
    };
  },
  methods: {
    querySotckList() {
      this.request
        .post("/getEligibleStockList", this.queryForm)
        .then((response) => {
          let data = response.data;
          this.tableData = data["list"];
          this.total = data["totalCount"];
        })
        .catch((error) => {
          if (error === "数据统计中") {
            alert(error);
          }
          this.tableData = [];
          this.total = 0;
        });
    },
    handleSizeChange(val) {
      this.queryForm.pageSize = val;
      this.querySotckList(1);
    },
    handleCurrentChange(val) {
      this.queryForm.pageIndex = val;
      this.querySotckList(val);
    },
    refer(row) {
      this.$router.push({
        name: "KLine",
        params: {
          id: row.stockId,
          stockName: row.stockName,
          stockCode: row.stockCode,
        },
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