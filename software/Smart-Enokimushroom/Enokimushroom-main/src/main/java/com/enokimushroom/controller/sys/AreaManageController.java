package com.enokimushroom.controller.sys;

import java.util.List;

import com.enokimushroom.common.core.domain.entity.SysUser;
import com.enokimushroom.common.utils.SecurityUtils;
import com.enokimushroom.entity.vo.AreaManageVo;
import jakarta.servlet.http.HttpServletResponse;
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
import com.enokimushroom.entity.AreaManage;
import com.enokimushroom.service.IAreaManageService;
import com.enokimushroom.common.utils.poi.ExcelUtil;
import com.enokimushroom.common.core.page.TableDataInfo;

/**
 * 【场地管理】Controller
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
@RestController
@RequestMapping("/system/area")
public class AreaManageController extends BaseController
{
    @Autowired
    private IAreaManageService areaManageService;

    /**
     * 查询【场地】列表
     */
    @PreAuthorize("@ss.hasPermi('system:area:list')")
    @GetMapping("/list")
    public TableDataInfo list(AreaManage areaManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        areaManage.setUserId(user.getUserId());
        startPage();
        List<AreaManage> list = areaManageService.selectAreaManageList(areaManage);
        return getDataTable(list);
    }

    /**
     * 查询【场地】列表
     */
    @PreAuthorize("@ss.hasPermi('system:area:list')")
    @GetMapping("/listByCount")
    public TableDataInfo listByCount()
    {
       List<AreaManageVo> list = areaManageService.selectAreaManageListByCount();
        return getDataTable(list);
    }

    /**
     * 导出【场地】列表
     */
    @PreAuthorize("@ss.hasPermi('system:area:export')")
    @Log(title = "【导出场地列表】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AreaManage areaManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        areaManage.setUserId(user.getUserId());
        List<AreaManage> list = areaManageService.selectAreaManageList(areaManage);
        ExcelUtil<AreaManage> util = new ExcelUtil<AreaManage>(AreaManage.class);
        util.exportExcel(response, list, "【导出场地】数据");
    }

    /**
     * 获取【场地】详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:area:query')")
    @GetMapping(value = "/{areaId}")
    public AjaxResult getInfo(@PathVariable("areaId") Long areaId)
    {
        return success(areaManageService.selectAreaManageByAreaId(areaId));
    }

    /**
     * 新增【场地】
     */
    @PreAuthorize("@ss.hasPermi('system:area:add')")
    @Log(title = "【新增场地】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AreaManage areaManage)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        areaManage.setUserId(user.getUserId());
        areaManage.setDelFlag(0L);
        return toAjax(areaManageService.insertAreaManage(areaManage));
    }

    /**
     * 修改【场地】
     */
    @PreAuthorize("@ss.hasPermi('system:area:edit')")
    @Log(title = "【修改场地】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AreaManage areaManage)
    {
        return toAjax(areaManageService.updateAreaManage(areaManage));
    }

    /**
     * 删除【场地】
     */
    @PreAuthorize("@ss.hasPermi('system:area:remove')")
    @Log(title = "【删除场地】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{areaIds}")
    public AjaxResult remove(@PathVariable Long[] areaIds)
    {
        return toAjax(areaManageService.deleteAreaManageByAreaIds(areaIds));
    }
}
