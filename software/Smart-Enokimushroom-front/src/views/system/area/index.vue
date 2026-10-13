<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="场地名称" prop="areaName">
        <el-input v-model="queryParams.areaName" placeholder="请输入场地名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="场地地址" prop="areaAddress">
        <el-input v-model="queryParams.areaAddress" placeholder="请输入场地地址" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="场地面积" prop="areaSquare">
        <el-input v-model="queryParams.areaSquare" placeholder="请输入场地面积" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="场地状态" prop="areaState">
        <el-input v-model="queryParams.areaState" placeholder="请输入场地状态：正常；停用；待建" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="场地户主" prop="areaLeader">
        <el-input v-model="queryParams.areaLeader" placeholder="请输入场地户主" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="联系方式" prop="areaPhone">
        <el-input v-model="queryParams.areaPhone" placeholder="请输入联系方式" clearable @keyup.enter.native="handleQuery" />
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
          v-hasPermi="['system:area:add']">新增场地</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['system:area:edit']">修改场地</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['system:area:remove']">删除场地</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['system:area:export']">导出场地</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="manageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" align="center" prop="areaId" />
      <el-table-column label="场地名称" align="center" prop="areaName" />
      <el-table-column label="场地地址" align="center" prop="areaAddress" />
      <el-table-column label="场地类型" align="center" prop="areaType">
        <template slot-scope="scope">
          <span v-if="scope.row.areaType === '企业'" style="color: green; font-weight: bold;">企业</span>
          <span v-else-if="scope.row.areaType === '个人'" style="color: red; font-weight: bold;">个人</span>
          <span v-else-if="scope.row.areaType === '众筹'" style="color: gray; font-weight: bold;">众筹</span>
          <span v-else style="font-weight: bold;">{{ scope.row.areaType }}</span>
        </template>
      </el-table-column>
      <el-table-column label="场地面积" align="center">
        <template slot-scope="scope">
          {{ scope.row.areaSquare }} 亩
        </template>
      </el-table-column>
      <el-table-column label="场地户主" align="center" prop="areaLeader" />
      <el-table-column label="联系方式" align="center" prop="areaPhone" />
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
      <el-table-column label="场地状态" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.areaState === '正常'" style="color: green; font-weight: bold;">正常</span>
          <span v-else-if="scope.row.areaState === '停用'" style="color: red; font-weight: bold;">停用</span>
          <span v-else-if="scope.row.areaState === '待见'" style="color: gray; font-weight: bold;">待见</span>
          <span v-else style="font-weight: bold;">{{ scope.row.areaState }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['system:area:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['system:area:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改【场地】对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="场地名称" prop="areaName">
          <el-input v-model="form.areaName" placeholder="请输入场地名称" />
        </el-form-item>
        <el-form-item label="场地地址" prop="areaAddress">
          <el-input v-model="form.areaAddress" placeholder="请输入场地地址" />
        </el-form-item>
        <el-form-item label="场地面积" prop="areaSquare">
          <el-input v-model="form.areaSquare" placeholder="请输入场地面积" style="width: 200px;" />
          <span style="margin-left: 8px;">亩</span>
        </el-form-item>
        <el-form-item label="场地类型" prop="areaType">
          <!-- <el-input v-model="form.areaType" placeholder="请输入场地类型：企业，个人，众筹" /> -->
          <el-select v-model="form.areaType" placeholder="请选择场地类型">
            <el-option v-for="item in optionsType" :key="item.value" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="场地状态" prop="areaState">
          <!-- <el-input v-model="form.areaState" placeholder="请输入场地状态：正常；停用；待建" /> -->
          <el-select v-model="form.areaState" placeholder="请选择场地状态">
            <el-option v-for="item in optionsState" :key="item.value" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="场地户主" prop="areaLeader">
          <el-input v-model="form.areaLeader" placeholder="请输入场地户主" />
        </el-form-item>
        <el-form-item label="联系方式" prop="areaPhone">
          <el-input v-model="form.areaPhone" placeholder="请输入11位联系方式" maxlength="11"
            oninput="value=value.replace(/[^0-9]/g,'')" />
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
        <!-- <el-form-item label="是否删除：0，未删除；1，删除" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入是否删除：0，未删除；1，删除" />
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
import { listManage, getManage, delManage, addManage, updateManage } from "@/api/system/area";
import { ElSelect, ElOption } from 'element-ui';

export default {
  name: "Area",
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
        areaName: null,
        areaAddress: null,
        areaType: null,
        areaSquare: null,
        areaState: null,
        areaLeader: null,
        areaPhone: null,
        startCreateDate: null,
        stopCreateDate: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        areaName: [
          { required: true, message: "场地名称不能为空", trigger: "blur" }
        ],
        // areaNumber: [
        //   { required: true, message: "场地编号不能为空", trigger: "blur" }
        // ],
        areaType: [
          { required: true, message: "场地类型不能为空", trigger: "blur" }
        ],
        areaState: [
          { required: true, message: "场地状态不能为空", trigger: "blur" }
        ],
        areaLeader: [
          { required: true, message: "联系人不能为空", trigger: "blur" }
        ],
        areaPhone: [
          { required: true, message: '请输入联系方式', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
        ],
      },
      //类型下拉框选择
      optionsType: [{
        value: '企业',
        label: '企业',
      }, {
        value: '个人',
        label: '个人',
      }, {
        value: '众筹',
        label: '众筹',
      }],
      //类型下拉框选择
      optionsState: [{
        value: '正常',
        label: '正常',
      }, {
        value: '停用',
        label: '停用',
      }, {
        value: '待建',
        label: '待建',
      }],
    };
  },
  created () {
    this.getList();
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
    // 取消按钮
    cancel () {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset () {
      this.form = {
        areaId: null,
        areaName: null,
        areaAddress: null,
        areaType: null,
        areaSquare: null,
        areaState: null,
        areaLeader: null,
        areaPhone: null,
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
      this.ids = selection.map(item => item.areaId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },

    //类型下拉选择框事件
    handleSelectedChangeType () {
      console.log(this.form.areaType);
    },

    //状态下拉选择框事件
    handleSelectedChangeState () {
      console.log(this.form.areaState);
    },

    /** 新增按钮操作 */
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = "添加场地";
    },
    /** 修改按钮操作 */
    handleUpdate (row) {
      this.reset();
      const areaId = row.areaId || this.ids
      getManage(areaId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改场地";
      });
    },
    /** 提交按钮 */
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.areaId != null) {
            updateManage(this.form).then(response => {
              console.log(this.form);
              console.log(response);
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addManage(this.form).then(response => {
              console.log(this.form);
              console.log(response);
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
      const areaIds = row.areaId || this.ids;
      this.$modal.confirm('是否确认删除【场地】编号为"' + areaIds + '"的数据项？').then(function () {
        return delManage(areaIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    /** 导出按钮操作 */
    handleExport () {
      this.download('system/area/export', {
        ...this.queryParams
      }, `area_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
