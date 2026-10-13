package com.enokimushroom.controller.sys;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

import com.enokimushroom.common.core.domain.entity.SysUser;
import com.enokimushroom.common.utils.SecurityUtils;
import com.enokimushroom.entity.AreaManage;
import com.enokimushroom.entity.vo.PlantManageVo;
import com.enokimushroom.service.IAreaManageService;
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
import com.enokimushroom.entity.PlantManage;
import com.enokimushroom.service.IPlantManageService;
import com.enokimushroom.common.utils.poi.ExcelUtil;
import com.enokimushroom.common.core.page.TableDataInfo;

/**
 * 【植物管理】Controller
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
@RestController
@RequestMapping("/system/plant")
public class PlantManageController extends BaseController
{
    @Autowired
    private IPlantManageService plantManageService;

    @Autowired
    private IAreaManageService areaManageService;

    /**
     * 查询【植物】列表
     */
    @PreAuthorize("@ss.hasPermi('system:plant:list')")
    @GetMapping("/list")
    public TableDataInfo list(PlantManage plantManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        plantManage.setUserId(user.getUserId());
        startPage();
        List<PlantManage> list = plantManageService.selectPlantManageList(plantManage);
        return getDataTable(list);
    }

    /**
     * 查询【植物数量、占比】列表
     */
    @PreAuthorize("@ss.hasPermi('system:plant:list')")
    @GetMapping("/listByCount")
    public TableDataInfo listByCount()
    {
        List<PlantManageVo> list = plantManageService.selectPlantManageListByCount();
        return getDataTable(list);
    }

    /**
     * 查询【场地列表】列表
     */
    @PreAuthorize("@ss.hasPermi('system:plant:list')")
    @GetMapping("/areaName")
    public TableDataInfo AreaNamelist(AreaManage areaManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        areaManage.setUserId(user.getUserId());
        startPage();
        List<AreaManage> list = areaManageService.selectAreaManageList(areaManage);
        return getDataTable(list);
    }

    /**
     * 导出【植物】列表
     */
    @PreAuthorize("@ss.hasPermi('system:plant:export')")
    @Log(title = "【导出植物】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PlantManage plantManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        plantManage.setUserId(user.getUserId());
        List<PlantManage> list = plantManageService.selectPlantManageList(plantManage);
        ExcelUtil<PlantManage> util = new ExcelUtil<PlantManage>(PlantManage.class);
        util.exportExcel(response, list, "【导出植物】数据");
    }

    /**
     * 获取【植物】详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:plant:query')")
    @GetMapping(value = "/{plantId}")
    public AjaxResult getInfo(@PathVariable("plantId") Long plantId)
    {
        return success(plantManageService.selectPlantManageByPlantId(plantId));
    }

    /**
     * 新增【植物】
     */
    @PreAuthorize("@ss.hasPermi('system:plant:add')")
    @Log(title = "【新增植物】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PlantManage plantManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        plantManage.setUserId(user.getUserId());
        plantManage.setDelFlag(0L);
        return toAjax(plantManageService.insertPlantManage(plantManage));
    }

    /**
     * 修改【植物】
     */
    @PreAuthorize("@ss.hasPermi('system:plant:edit')")
    @Log(title = "【修改植物】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PlantManage plantManage)
    {
        return toAjax(plantManageService.updatePlantManage(plantManage));
    }

    /**
     * 删除【植物】
     */
    @PreAuthorize("@ss.hasPermi('system:plant:remove')")
    @Log(title = "【修改植物】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{plantIds}")
    public AjaxResult remove(@PathVariable Long[] plantIds)
    {
        return toAjax(plantManageService.deletePlantManageByPlantIds(plantIds));
    }
}
