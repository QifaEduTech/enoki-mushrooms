<template>
<div class="center_bottom">
    <div class="server-info">
        <dv-scroll-board :config="config" style="width:100%;height:180px" />
    </div>
    <div class="server-metrics">
        <div class="metric-item">
            <div class="metric-ring">
                <dv-water-level-pond :config="configCpu" style="width:100%;height:100%;" />
            </div>
            <div class="metric-label">CPU</div>
        </div>
        <div class="metric-item">
            <div class="metric-ring">
                <dv-water-level-pond :config="configMemery" style="width:100%;height:100%;" />
            </div>
            <div class="metric-label">内存</div>
        </div>
        <div class="metric-item">
            <div class="metric-ring">
                <dv-water-level-pond :config="configDisk" style="width:100%;height:100%;" />
            </div>
            <div class="metric-label">系统盘</div>
        </div>
    </div>
</div>
</template>

<script>
export default {
    data() {
        return {
            timer: null,
            config: {},
            server: {
                jvm: { name: "", version: "", startTime: "", runTime: "", used: "", total: 100 },
                sys: { computerName: "", osName: "", computerIp: "", osArch: "" },
                cpu: { cpuNum: 1 },
                mem: { total: 2 }
            },
            configCpu: { data: [42.3], shape: 'roundRect', formatter: '{value}%', waveHeight: 10 },
            configMemery: { data: [40.6], shape: 'roundRect', formatter: '{value}%', waveHeight: 10 },
            configDisk: { data: [50.0], shape: 'roundRect', formatter: '{value}%', waveHeight: 10 }
        };
    },
    mounted() {
        this.getServer();
    },
    beforeDestroy() {
        this.clearData();
    },
    methods: {
        getServer() {
            this.server = {
                jvm: {
                    name: 'Java HotSpot(TM) 64-Bit Server VM',
                    version: '1.8.0_281',
                    startTime: '2025-10-26 08:00:00',
                    runTime: '4h 12m',
                    used: '6.52GB',
                    total: '16GB'
                },
                sys: {
                    computerName: 'dev-server-01',
                    osName: 'Windows Server 2019',
                    computerIp: '192.168.1.100',
                    osArch: 'x64'
                },
                cpu: { cpuNum: 8, used: 32.1, sys: 10.2, free: 57.7 },
                mem: { total: '16GB', used: 6.52, free: 9.48 },
                sysFiles: [{ used: '250GB', free: '250GB' }]
            };

            this.config = {
                rowNum: 5,
                oddRowBGC: 'rgba(0, 114, 255, 0.04)',
                evenRowBGC: 'rgba(0, 234, 255, 0.02)',
                columnWidth: [110, 250],
                align: ['left', 'left'],
                data: [
                    ['服务器名：', this.server.sys.computerName],
                    ['服务器IP：', this.server.sys.computerIp],
                    ['操作系统：', this.server.sys.osName],
                    ['系统架构：', this.server.sys.osArch],
                    ['CPU核心：', this.server.cpu.cpuNum],
                    ['系统内存：', this.server.mem.total],
                    ['Java名称：', this.server.jvm.name],
                    ['Java版本：', this.server.jvm.version],
                    ['启动时间：', this.server.jvm.startTime],
                    ['运行时长：', this.server.jvm.runTime],
                    ['运行内存：', this.server.jvm.used],
                    ['JVM总内存：', this.server.jvm.total],
                ],
            };

            let cpu = (this.server.cpu.used + this.server.cpu.sys) / (this.server.cpu.used + this.server.cpu.sys + this.server.cpu.free) * 100;
            this.configCpu = { data: [cpu.toFixed(1)], shape: 'roundRect', formatter: '{value}%', waveHeight: 10 };

            let memery = this.server.mem.used / (this.server.mem.used + this.server.mem.free) * 100;
            this.configMemery = { data: [memery.toFixed(1)], shape: 'roundRect', formatter: '{value}%', waveHeight: 10 };

            let disk = Number(this.server.sysFiles[0].used.replace("GB", "")) / (Number(this.server.sysFiles[0].used.replace("GB", "")) + Number(this.server.sysFiles[0].free.replace("GB", ""))) * 100;
            this.configDisk = { data: [disk.toFixed(1)], shape: 'roundRect', formatter: '{value}%', waveHeight: 10 };
        },
        clearData() {
            if (this.timer) {
                clearInterval(this.timer);
                this.timer = null;
            }
        },
    }
}
</script>

<style lang="scss" scoped>
.center_bottom {
    width: 100%;
    height: 100%;
    display: flex;
    gap: 16px;
    padding: 4px 0;
}

.server-info {
    flex: 1;
    min-width: 0;
}

.server-metrics {
    display: flex;
    gap: 16px;
    align-items: center;
    padding-right: 4px;

    .metric-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        .metric-ring {
            width: 90px;
            height: 90px;
        }

        .metric-label {
            font-size: 13px;
            font-weight: 600;
            color: rgba(0, 234, 255, 0.85);
            letter-spacing: 2px;
        }
    }
}
</style>
