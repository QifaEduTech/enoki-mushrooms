<template>
  <div class="trace-page">
    <div class="page-heading">
      <div>
        <h2>溯源批次详情</h2>
        <p>主系统 MySQL 实时溯源档案</p>
      </div>
      <div class="heading-actions">
        <el-select v-model="selectedBatchCode" class="batch-select" placeholder="选择批次" @change="changeBatch">
          <el-option v-for="batch in batches" :key="batch.trace_code" :label="`${batch.trace_code} · ${batch.product_name || batch.plant_name || '未命名产品'}`" :value="batch.trace_code" />
        </el-select>
        <el-tag :type="connected ? 'success' : 'danger'" effect="plain">
          {{ connected ? `溯源服务已连接 · ${servicePort}` : '溯源服务未连接' }}
        </el-tag>
        <el-button :loading="loading" icon="el-icon-refresh" @click="refreshTraceability">刷新批次数据</el-button>
        <el-button type="success" icon="el-icon-download" :disabled="!qrImageUrl" @click="downloadQr">下载二维码</el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="overview-card" shadow="never">
      <div class="batch-overview">
        <div class="overview-main">
          <div class="overview-grid">
            <div v-for="item in overviewItems" :key="item.label" class="overview-item">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
          <div class="overview-grid secondary">
            <div v-for="item in secondaryItems" :key="item.label" class="overview-item">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>
        <div class="batch-qr">
          <span class="qr-title">批次溯源码</span>
          <div class="qr-frame" :class="{ 'is-generating': qrGenerating, 'is-generated': qrGenerated }">
            <img v-if="qrImageUrl" :src="qrImageUrl" alt="批次溯源二维码" @load="onQrLoaded" @error="onQrError">
            <i v-else-if="!qrGenerating" class="el-icon-picture-outline" />
            <div v-if="qrGenerating" class="qr-generating">
              <span class="qr-loader"><i class="el-icon-refresh" /></span>
              <small>正在生成二维码</small>
            </div>
            <span v-if="qrGenerated && !qrGenerating" class="qr-success"><i class="el-icon-check" /></span>
            <span v-if="qrImageUrl && !qrGenerating" class="qr-scan-line" />
          </div>
          <small class="qr-status" :class="{ success: qrImageUrl && !qrGenerating }">{{ qrGenerating ? '正在生成…' : (qrImageUrl ? '二维码已生成' : '等待生成') }}</small>
          <el-button v-if="!qrImageUrl" type="success" size="mini" icon="el-icon-circle-plus-outline" :loading="qrGenerating" :disabled="loading || !status.bag_code" @click="generateQr">生成二维码</el-button>
          <el-button type="text" icon="el-icon-download" :disabled="!qrImageUrl" @click="downloadQr">下载二维码</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="chain-card" shadow="never">
      <div slot="header" class="section-title"><span>溯源链路</span><small>数据随数据库同步刷新</small></div>
      <div class="trace-chain">
        <template v-for="(node, index) in chainNodes">
          <div :key="node.name" class="chain-node">
            <div class="chain-icon"><i :class="node.icon" /></div>
            <h4>{{ node.name }}</h4>
            <div class="chain-detail">
              <p><span>时间</span>{{ node.time }}</p>
              <p><span>地点</span>{{ node.place }}</p>
              <p><span>信息</span>{{ node.detail }}</p>
              <el-tag size="mini" type="success">已记录</el-tag>
            </div>
          </div>
          <div v-if="index < chainNodes.length - 1" :key="`${node.name}-arrow`" class="chain-arrow"><i class="el-icon-right" /></div>
        </template>
      </div>
    </el-card>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="8">
        <el-card class="detail-card" shadow="never">
          <div slot="header" class="section-title"><span>批次基础信息</span></div>
          <div class="detail-list">
            <div v-for="item in baseItems" :key="item.label"><span>{{ item.label }}</span><strong>{{ item.value }}</strong></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="detail-card" shadow="never">
          <div slot="header" class="section-title"><span>质检报告</span><el-tag size="mini" type="success">{{ profile.report ? '已检测' : '待检测' }}</el-tag></div>
          <div class="detail-list">
            <div><span>报告编号</span><strong>{{ value(profile.report_no) }}</strong></div>
            <div><span>质检人员</span><strong>{{ value(profile.inspector) }}</strong></div>
            <div><span>检测日期</span><strong>{{ formatDate(profile.inspect_date) }}</strong></div>
            <div><span>检测项目</span><strong>{{ value(profile.inspection_items) }}</strong></div>
            <div class="wide"><span>检测结论</span><strong>{{ value(profile.report) }}</strong></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="detail-card" shadow="never">
          <div slot="header" class="section-title"><span>销售与信用</span></div>
          <div class="detail-list">
            <div><span>销售客户</span><strong>{{ value(sale.customer) }}</strong></div>
            <div><span>销售地点</span><strong>{{ value(sale.location) }}</strong></div>
            <div><span>销售日期</span><strong>{{ formatDate(sale.sale_date) }}</strong></div>
            <div><span>销售数量</span><strong>{{ value(sale.quantity) }}</strong></div>
            <div><span>信用等级</span><strong class="success-text">{{ value(profile.credit_level) }}</strong></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card class="table-card" shadow="never">
          <div slot="header" class="section-title"><span>最新环境监测数据</span><small>最近 {{ environmentRows.length }} 条</small></div>
          <el-table :data="environmentRows" size="small" stripe max-height="330">
            <el-table-column prop="collect_time" label="采集时间" min-width="165" />
            <el-table-column prop="temp" label="空气温度(℃)" width="115" />
            <el-table-column prop="humidity" label="空气湿度(%)" width="115" />
            <el-table-column prop="co2" label="CO₂(ppm)" width="105" />
            <el-table-column prop="soil_temperature" label="土壤温度(℃)" width="115" />
            <el-table-column prop="soil_ph" label="土壤PH" width="90" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="timeline-card" shadow="never">
          <div slot="header" class="section-title"><span>流转记录时间轴</span></div>
          <el-timeline>
            <el-timeline-item v-for="item in timelineRows" :key="`${item.name}-${item.start_time}`" :timestamp="formatDateTime(item.start_time || item.date)" color="#16a567" placement="top">
              <strong>{{ item.name }}</strong>
              <p>{{ item.area || item.operator || item.notes }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-alert v-if="errorMessage" class="error-alert" :title="errorMessage" type="error" show-icon :closable="false" />
  </div>
</template>

<script>
import axios from 'axios'

const traceClient = axios.create({ timeout: 8000 })

export default {
  name: 'Traceability',
  data() {
    return {
      serviceUrl: localStorage.getItem('traceability-service-url') || 'http://127.0.0.1:8000',
      loading: false,
      connected: false,
      batches: [],
      selectedBatchCode: '',
      status: {},
      qrImageUrl: '',
      qrGenerating: false,
      qrGenerated: false,
      qrSuccessTimer: null,
      errorMessage: ''
    }
  },
  computed: {
    baseUrl() { return this.serviceUrl.replace(/\/$/, '') },
    servicePort() { try { return new URL(this.baseUrl).port || '80' } catch (e) { return '-' } },
    profile() { return this.status.profile || {} },
    snapshot() { return this.status.snapshot || {} },
    sale() { return this.profile.sale || {} },
    environmentRows() { return (this.snapshot.environment || []).slice().reverse() },
    timelineRows() { return (this.profile.timeline || []).slice().reverse() },
    overviewItems() {
      return [
        { label: '批次编号', value: this.status.bag_code || this.selectedBatchCode },
        { label: '产品名称', value: this.value(this.profile.product_name) },
        { label: '所属基地', value: this.value(this.profile.company) },
        { label: '生产/建档日期', value: this.formatDate(this.profile.produce_date) },
        { label: '当前状态', value: this.status.success ? '溯源数据正常' : '等待数据' }
      ]
    },
    secondaryItems() {
      return [
        { label: '关联监测数据', value: `${(this.snapshot.environment || []).length} 条` },
        { label: '负责人', value: this.value(this.profile.producer) },
        { label: '包装日期', value: this.formatDate(this.profile.pack_date) },
        { label: '销售日期', value: this.formatDate(this.sale.sale_date) }
      ]
    },
    baseItems() {
      return [
        { label: '产品品种', value: this.value(this.profile.strain_name) },
        { label: '生产基地', value: this.value(this.profile.base_area) },
        { label: '基地地址', value: this.value(this.profile.origin) },
        { label: '负责人', value: this.value(this.profile.producer) },
        { label: '联系电话', value: this.maskPhone(this.profile.phone) },
        { label: '包装数量', value: this.profile.pack_qty ? `${this.profile.pack_qty} 件` : '—' },
        { label: '包装重量', value: this.profile.pack_weight ? `${this.profile.pack_weight} kg` : '—' }
      ]
    },
    chainNodes() {
      const timeline = this.profile.timeline || []
      const planting = timeline.find(item => item.name && item.name.includes('档案')) || {}
      const device = timeline.find(item => item.name && item.name.includes('设备')) || {}
      const quality = timeline.find(item => item.name && item.name.includes('检测')) || {}
      return [
        { name: '种植建档', icon: 'el-icon-s-platform', time: this.formatDateTime(planting.start_time || this.profile.produce_date), place: this.value(planting.area || this.profile.company), detail: this.value(planting.operator || this.profile.producer) },
        { name: '设备监测', icon: 'el-icon-monitor', time: this.formatDateTime(device.start_time), place: this.value(device.area), detail: `${(this.snapshot.devices || []).length} 台设备接入` },
        { name: '包装入库', icon: 'el-icon-box', time: this.formatDate(this.profile.pack_date), place: this.value(this.profile.company), detail: this.profile.pack_qty ? `${this.profile.pack_qty} 件 / ${this.profile.pack_weight || 0} kg` : '已记录' },
        { name: '销售流通', icon: 'el-icon-truck', time: this.formatDate(this.sale.sale_date), place: this.value(this.sale.location), detail: this.value(this.sale.customer) },
        { name: '质量检测', icon: 'el-icon-document-checked', time: this.formatDateTime(quality.start_time || this.profile.inspect_date), place: this.value(quality.area || this.profile.company), detail: this.value(this.profile.report) },
        { name: '消费者查询', icon: 'el-icon-user', time: '实时', place: '扫码访问', detail: '查看当前批次溯源档案' }
      ]
    }
  },
  methods: {
    value(value) { return value === null || value === undefined || value === '' ? '—' : value },
    formatDate(value) { return value ? String(value).slice(0, 10) : '—' },
    formatDateTime(value) { return value ? String(value).replace('T', ' ').replace(/\.\d+$/, '').slice(0, 19) : '—' },
    maskPhone(value) { return value && value.length >= 7 ? `${value.slice(0, 3)}****${value.slice(-4)}` : this.value(value) },
    async discoverService() {
      const saved = localStorage.getItem('traceability-service-url')
      const candidates = [saved, ...Array.from({ length: 11 }, (_, index) => `http://127.0.0.1:${8000 + index}`)].filter((value, index, list) => value && list.indexOf(value) === index)
      for (const candidate of candidates) {
        try {
          const response = await traceClient.get(`${candidate.replace(/\/$/, '')}/api/health`, { timeout: 900 })
          if (response.data && response.data.mode === 'main-system-data') { this.serviceUrl = candidate; return true }
        } catch (e) {}
      }
      return false
    },
    async loadStatus() {
      if (!this.connected) await this.discoverService()
      const response = await traceClient.get(`${this.baseUrl}/api/status?bag_code=${encodeURIComponent(this.selectedBatchCode)}`)
      if (!response.data.success) throw new Error(response.data.message || '读取溯源数据失败')
      this.status = response.data
      this.connected = true
      localStorage.setItem('traceability-service-url', this.baseUrl)
      localStorage.setItem('traceability-batch-code', this.selectedBatchCode)
    },
    async loadBatches() {
      const response = await traceClient.get(`${this.baseUrl}/api/batches`)
      if (!response.data.success) throw new Error(response.data.message || '读取批次列表失败')
      this.batches = response.data.batches || []
      if (!this.batches.length) throw new Error('主系统暂无可用批次')
      if (!this.batches.some(item => item.trace_code === this.selectedBatchCode)) this.selectedBatchCode = this.batches[0].trace_code
    },
    async generateQr() {
      this.qrGenerating = true
      this.qrGenerated = false
      this.qrImageUrl = ''
      clearTimeout(this.qrSuccessTimer)
      try {
        const response = await traceClient.post(`${this.baseUrl}/api/qr/refresh`, { bag_code: this.selectedBatchCode })
        if (!response.data.success) throw new Error(response.data.message || '二维码生成失败')
        this.qrImageUrl = `${this.baseUrl}/static/trace-qr.png?bag_code=${encodeURIComponent(this.selectedBatchCode)}&t=${Date.now()}`
      } catch (error) {
        this.qrGenerating = false
        throw error
      }
    },
    onQrLoaded() {
      this.qrGenerating = false
      this.qrGenerated = true
      this.qrSuccessTimer = setTimeout(() => { this.qrGenerated = false }, 1800)
    },
    onQrError() {
      this.qrGenerating = false
      this.qrGenerated = false
      this.qrImageUrl = ''
    },
    async refreshTraceability(showMessage = true) {
      this.loading = true
      this.errorMessage = ''
      try {
        if (!this.connected) await this.discoverService()
        await this.loadBatches()
        await this.loadStatus()
        this.qrImageUrl = ''
        this.qrGenerated = false
        if (showMessage) this.$message.success('批次数据已刷新，请点击生成二维码')
      } catch (error) {
        this.connected = false
        this.errorMessage = `无法连接溯源服务：${error.message || '请重新运行主项目启动器'}`
      } finally {
        this.loading = false
      }
    },
    changeBatch() {
      this.status = {}
      this.qrImageUrl = ''
      this.refreshTraceability(false)
    },
    async downloadQr() {
      try {
        const link = document.createElement('a')
        link.href = `${this.baseUrl}/api/qr/download?bag_code=${encodeURIComponent(this.selectedBatchCode)}&t=${Date.now()}`
        link.download = `溯源二维码-${this.selectedBatchCode}.png`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        this.$message.success('二维码已下载')
      } catch (error) {
        this.$message.error('二维码下载失败，请先刷新数据')
      }
    }
  },
  mounted() { this.refreshTraceability(false) },
  beforeDestroy() { clearTimeout(this.qrSuccessTimer) }
}
</script>

<style lang="scss" scoped>
.trace-page { min-height: calc(100vh - 84px); padding: 18px; background: #f4f7f8; color: #263238; }
.page-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.page-heading h2 { margin: 0; font-size: 22px; }
.page-heading p { margin: 6px 0 0; color: #8b9892; font-size: 13px; }
.heading-actions { display: flex; align-items: center; gap: 10px; }
.batch-select { width: 230px; }
.overview-card, .chain-card, .detail-card, .table-card, .timeline-card { border: 1px solid #e4ebe7; border-radius: 8px; }
.overview-card, .chain-card, .content-row { margin-bottom: 16px; }
.batch-overview { display: flex; min-height: 190px; }
.overview-main { flex: 1; min-width: 0; }
.overview-grid { display: grid; grid-template-columns: repeat(5, minmax(120px, 1fr)); }
.overview-grid.secondary { margin-top: 24px; padding-top: 20px; border-top: 1px solid #edf1ef; }
.overview-item { min-height: 62px; padding: 4px 20px; border-right: 1px solid #edf1ef; }
.overview-item:last-child { border-right: none; }
.overview-item span, .detail-list span { display: block; margin-bottom: 10px; color: #8c9993; font-size: 13px; }
.overview-item strong { font-size: 16px; font-weight: 600; line-height: 1.55; word-break: break-word; }
.batch-qr { width: 200px; margin-left: 18px; padding: 8px 18px; border: 1px solid #e1e9e4; border-radius: 8px; text-align: center; background: #fbfdfc; }
.qr-title { display: block; margin-bottom: 7px; font-weight: 600; }
.qr-frame { position: relative; display: flex; align-items: center; justify-content: center; width: 126px; height: 126px; margin: auto; overflow: hidden; border: 1px solid transparent; border-radius: 5px; background: #fff; transition: border-color .25s, box-shadow .25s; }
.qr-frame.is-generating { border-color: #a8dfc2; background: linear-gradient(135deg, #f7fffa, #edf9f3); }
.qr-frame.is-generated { border-color: #20b66a; box-shadow: 0 0 0 4px rgba(32, 182, 106, .12); }
.qr-frame img { width: 118px; height: 118px; object-fit: contain; animation: qrReveal .38s ease-out; }
.qr-frame i { color: #c7d2cc; font-size: 54px; }
.qr-generating { position: absolute; inset: 0; z-index: 2; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 9px; color: #168b55; background: rgba(241, 252, 246, .94); }
.qr-generating small { font-size: 12px; }
.qr-loader { display: flex; align-items: center; justify-content: center; width: 42px; height: 42px; border-radius: 50%; background: #e2f6eb; }
.qr-loader i { color: #18a760; font-size: 23px; animation: qrRotate .85s linear infinite; }
.qr-success { position: absolute; top: 5px; right: 5px; display: flex; align-items: center; justify-content: center; width: 25px; height: 25px; border-radius: 50%; color: #fff; background: #20b66a; animation: qrSuccess .35s ease-out; }
.qr-success i { color: #fff; font-size: 15px; }
.qr-scan-line { position: absolute; left: 7px; right: 7px; height: 2px; background: linear-gradient(90deg, transparent, #20b66a, transparent); box-shadow: 0 0 7px #20b66a; opacity: 0; animation: qrScan 1.6s ease-in-out 2 forwards; }
.qr-status { display: block; height: 18px; margin-top: 5px; color: #9aa69f; font-size: 11px; }
.qr-status.success { color: #16a05b; }
@keyframes qrRotate { to { transform: rotate(360deg); } }
@keyframes qrReveal { from { opacity: 0; transform: scale(.86); } to { opacity: 1; transform: scale(1); } }
@keyframes qrSuccess { from { opacity: 0; transform: scale(.4); } to { opacity: 1; transform: scale(1); } }
@keyframes qrScan { 0% { top: 9px; opacity: 0; } 15% { opacity: .85; } 85% { opacity: .85; } 100% { top: 115px; opacity: 0; } }
.section-title { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.section-title small { color: #91a099; font-weight: 400; }
.trace-chain { display: flex; align-items: flex-start; overflow-x: auto; padding: 2px 4px 8px; }
.chain-node { flex: 1; min-width: 172px; text-align: center; }
.chain-icon { display: flex; align-items: center; justify-content: center; width: 52px; height: 52px; margin: 0 auto 8px; border-radius: 50%; color: #0c9b5b; background: #e9f8f0; border: 1px solid #c6ead7; font-size: 24px; }
.chain-node h4 { margin: 6px 0 10px; color: #087b49; }
.chain-detail { padding: 12px; border: 1px solid #e5ece8; border-radius: 7px; text-align: left; background: #fff; }
.chain-detail p { margin: 0 0 8px; color: #5e6b65; font-size: 12px; line-height: 1.45; }
.chain-detail p span { display: inline-block; width: 34px; color: #9aa69f; }
.chain-arrow { flex: 0 0 42px; padding-top: 17px; color: #54bd87; font-size: 26px; text-align: center; }
.detail-card { min-height: 330px; margin-bottom: 16px; }
.detail-list > div { display: grid; grid-template-columns: 92px 1fr; padding: 10px 0; border-bottom: 1px dashed #edf1ef; }
.detail-list > div:last-child { border-bottom: 0; }
.detail-list span { margin: 0; }
.detail-list strong { font-size: 14px; font-weight: 500; line-height: 1.5; word-break: break-word; }
.success-text { color: #0b9a5b; }
.table-card, .timeline-card { min-height: 410px; }
.timeline-card ::v-deep .el-card__body { max-height: 350px; overflow-y: auto; }
.timeline-card p { margin: 6px 0 0; color: #7d8983; font-size: 12px; line-height: 1.5; }
.error-alert { margin-top: 12px; }
@media (max-width: 1200px) { .overview-grid { grid-template-columns: repeat(3, 1fr); row-gap: 18px; } }
@media (max-width: 768px) { .trace-page { padding: 10px; } .page-heading, .batch-overview { align-items: stretch; flex-direction: column; } .heading-actions { flex-wrap: wrap; margin-top: 12px; } .overview-grid { grid-template-columns: repeat(2, 1fr); } .batch-qr { width: auto; margin: 18px 0 0; } }
</style>
