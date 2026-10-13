package com.enokimushroom.controller.sys;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

import com.enokimushroom.common.core.domain.entity.SysUser;
import com.enokimushroom.common.utils.SecurityUtils;
import com.enokimushroom.entity.AreaManage;
import com.enokimushroom.entity.PlantManage;
import com.enokimushroom.entity.vo.DeviceManageVo;
import com.enokimushroom.service.IAreaManageService;
import com.enokimushroom.service.IPlantManageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enokimushroom.common.annotation.Log;
import com.enokimushroom.common.core.controller.BaseController;
import com.enokimushroom.common.core.domain.AjaxResult;
import com.enokimushroom.common.enums.BusinessType;
import com.enokimushroom.entity.DeviceManage;
import com.enokimushroom.service.IDeviceManageService;
import com.enokimushroom.common.utils.poi.ExcelUtil;
import com.enokimushroom.common.core.page.TableDataInfo;


@RestController
@RequestMapping("/system/device")
public class DeviceManageController extends BaseController
{
    @Autowired
    private IDeviceManageService deviceManageService;

    @Autowired
    private IPlantManageService plantManageService;

    @Autowired
    private IAreaManageService areaManageService;

    /**
     * 查询【设备】列表
     */
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceManage deviceManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        deviceManage.setUserId(user.getUserId());
        startPage();
        List<DeviceManage> list = deviceManageService.selectDeviceManageList(deviceManage);
        return getDataTable(list);
    }

    /**
     * 查询【设备】列表
     */
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/listByCount")
    public TableDataInfo listByCount()
    {
        List<DeviceManageVo> list = deviceManageService.selectDeviceManageListByCount();
        return getDataTable(list);
    }

    /**
     * 导出【设备】列表
     */
    @PreAuthorize("@ss.hasPermi('system:device:export')")
    @Log(title = "【导出设备】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DeviceManage deviceManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        deviceManage.setUserId(user.getUserId());
        List<DeviceManage> list = deviceManageService.selectDeviceManageList(deviceManage);
        ExcelUtil<DeviceManage> util = new ExcelUtil<DeviceManage>(DeviceManage.class);
        util.exportExcel(response, list, "【导出设备】数据");
    }

    /**
     * 获取【设备】详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:device:query')")
    @GetMapping(value = "/{deviceId}")
    public AjaxResult getInfo(@PathVariable("deviceId") Long deviceId)
    {
        return success(deviceManageService.selectDeviceManageByDeviceId(deviceId));
    }

    /**
     * 新增【设备】
     */
    @PreAuthorize("@ss.hasPermi('system:device:add')")
    @Log(title = "【新增设备】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceManage deviceManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        deviceManage.setUserId(user.getUserId());
        deviceManage.setDelFlag(0L);
        return toAjax(deviceManageService.insertDeviceManage(deviceManage));
    }

    /**
     * 修改【设备】
     */
    @PreAuthorize("@ss.hasPermi('system:device:edit')")
    @Log(title = "【修改设备】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceManage deviceManage)
    {
        return toAjax(deviceManageService.updateDeviceManage(deviceManage));
    }

    /**
     * 删除【设备】
     */
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @Log(title = "【删除管理】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable Long[] deviceIds)
    {
        return toAjax(deviceManageService.deleteDeviceManageByDeviceIds(deviceIds));
    }

    /**
     * 查询【植物】列表
     */
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/plantId")
    public TableDataInfo getPlantIdlist(PlantManage plantManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        plantManage.setUserId(user.getUserId());
        startPage();
        List<PlantManage> list = plantManageService.selectPlantManageList(plantManage);
        return getDataTable(list);
    }

    /**
     * 查询【场地列表】列表
     */
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/areaName")
    public TableDataInfo AreaNamelist(AreaManage areaManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        areaManage.setUserId(user.getUserId());
        startPage();
        List<AreaManage> list = areaManageService.selectAreaManageList(areaManage);
        return getDataTable(list);
    }
}
