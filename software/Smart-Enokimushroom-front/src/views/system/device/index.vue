<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="设备编号" prop="deviceNumber">
        <el-input v-model="queryParams.deviceNumber" placeholder="请输入设备编号" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="监控植物" prop="plantId">
        <el-input v-model="queryParams.plantId" placeholder="请输入监控植物" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="所属场地" prop="areaId">
        <el-input v-model="queryParams.areaId" placeholder="请输入所属场地" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备状态" prop="deviceState">
        <el-input v-model="queryParams.deviceState" placeholder="请输入设备状态：1，正常使用；0，停用" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <!-- <el-form-item label="开始创建时间" prop="startCreateDate">
        <el-date-picker clearable v-model="queryParams.startCreateDate" type="date" value-format="yyyy-MM-dd"
          placeholder="请选择开始创建时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束创建时间" prop="stopCreateDate">
        <el-date-picker clearable v-model="queryParams.stopCreateDate" type="date" value-format="yyyy-MM-dd"
          placeholder="请选择结束创建时间">
        </el-date-picker>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['system:device:add']">新增设备</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['system:device:edit']">修改设备</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['system:device:remove']">删除设备</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['system:device:export']">导出设备列表</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="manageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" align="center" prop="deviceId" />
      <el-table-column label="设备名称" align="center" prop="deviceName" />
      <el-table-column label="设备编号" align="center" prop="deviceNumber" />
      <el-table-column label="设备类型" align="center" prop="deviceType" />
      <!-- <el-table-column label="监控植物" align="center" prop="plantId" /> -->
      <el-table-column label="监控植物" align="center" prop="plantName" />
      <!-- <el-table-column label="所属场地" align="center" prop="areaId" /> -->
      <el-table-column label="所属场地" align="center" prop="areaName" />
      <el-table-column label="设备状态" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.deviceState === 1" style="color: green; font-weight: bold;">
            正常使用
          </span>
          <span v-else style="color: red; font-weight: bold;">
            停用
          </span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['system:device:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['system:device:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改【设备】对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="设备编号" prop="deviceNumber">
          <el-input v-model="form.deviceNumber" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceName">
          <el-input v-model="form.deviceType" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备状态" prop="deviceState">
          <!-- <el-input v-model="form.deviceState" placeholder="请输入设备状态：1，正常使用；0，停用" /> -->
          <el-select v-model="form.deviceState" placeholder="请选择设备状态：1，正常使用；0，停用">
            <el-option v-for="item in optionsDeviceState" :key="item.value" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="监控植物" prop="plantId">
          <!-- <el-input v-model="form.plantId" placeholder="请输入监控植物" /> -->
          <el-select v-model="form.plantId" placeholder="请选择监控植物">
            <el-option v-for="item in plantList" :key="item.plantId" :label="item.plantName" :value="item.plantId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属场地" prop="areaId">
          <!-- <el-input v-model="form.areaId" placeholder="请输入所属场地" /> -->
          <el-select v-model="form.areaId" placeholder="请选择所属场地">
            <el-option v-for="item in areaList" :key="item.areaId" :label="item.areaName" :value="item.areaId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间" prop="createDate">
          <el-date-picker clearable v-model="form.createDate" type="date" value-format="yyyy-MM-dd"
            placeholder="请选择创建时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="更新时间" prop="updateDate">
          <el-date-picker clearable v-model="form.updateDate" type="date" value-format="yyyy-MM-dd"
            placeholder="请选择更新时间">
          </el-date-picker>
        </el-form-item>
        <!-- <el-form-item label="是否删除：0，未删除，1删除" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入是否删除：0，未删除，1删除" />
        </el-form-item> -->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listManage, getManage, delManage, addManage, updateManage, getPlantId, getAreaName } from "@/api/system/device";
import { listArea } from "@/api/system/area";
import { listPlant } from "@/api/system/plant";

export default {
  name: "Device",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 【请填写功能名称】表格数据
      manageList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceNumber: null,
        deviceType: null,
        plantId: null,
        areaId: null,
        deviceState: null,
        deviceName: null,
        createDate: null,
        startCreateDate: null,
        stopCreateDate: null,
        updateDate: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        deviceNumber: [
          { required: true, message: "设备编号不能为空", trigger: "blur" }
        ],
        deviceName: [
          { required: true, message: "设备名称不能为空", trigger: "blur" }
        ],
        // deviceType: [
        //   { required: true, message: "设备类型不能为空", trigger: "blur" }
        // ],
        deviceState: [
          { required: true, message: "设备状态不能为空", trigger: "blur" }
        ],
        plantId: [
          { required: true, message: "监控植物不能为空", trigger: "blur" }
        ],
        areaId: [
          { required: true, message: "监控场地不能为空", trigger: "blur" }
        ],
      },
      //设备状态选项
      optionsDeviceState: [{
        value: '1',
        label: '正常使用',
      }, {
        value: '0',
        label: '停用',
      },],
      //监控植物选项
      plantList: [],
      //场地下拉框选择
      areaList: [],
    };
  },
  created () {
    this.getList();
  },
  mounted () {
    this.getArea();
    this.getPlant();
  },
  methods: {
    /** 查询【请填写功能名称】列表 */
    getList () {
      this.loading = true;
      listManage(this.queryParams).then(response => {
        this.manageList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 查询场地列表 */
    getArea () {
      listArea(this.queryParams).then(response => {
        if (response.code === 200) {
          this.areaList = response.rows;
          //console.log(this.areaList);
        } else {
          this.$modal.msgError("读取场地信息失败！！！");
        }

      });
    },
    /** 查询植物列表 */
    getPlant () {
      listPlant(this.queryParams).then(response => {
        if (response.code === 200) {
          this.plantList = response.rows;
          //console.log(this.plantList);
        } else {
          this.$modal.msgError("读取植物信息失败！！！");
        }

      });
    },
    // 取消按钮
    cancel () {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset () {
      this.form = {
        deviceId: null,
        deviceNumber: null,
        deviceType: null,
        plantId: null,
        areaId: null,
        deviceState: null,
        deviceName: null,
        createDate: null,
        startCreateDate: null,
        stopCreateDate: null,
        createBy: null,
        updateDate: null,
        updateBy: null,
        delFlag: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.deviceId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = "添加【设备】";
    },
    /** 修改按钮操作 */
    handleUpdate (row) {
      this.reset();
      const deviceId = row.deviceId || this.ids
      getManage(deviceId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改【设备】";
      });
    },
    /** 提交按钮 */
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.deviceId != null) {
            updateManage(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addManage(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete (row) {
      const deviceIds = row.deviceId || this.ids;
      this.$modal.confirm('是否确认删除【设备】编号为"' + deviceIds + '"的数据项？').then(function () {
        return delManage(deviceIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    /** 导出按钮操作 */
    handleExport () {
      this.download('system/device/export', {
        ...this.queryParams
      }, `device${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
