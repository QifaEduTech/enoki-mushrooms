package com.enokimushroom.controller.ai;

import com.enokimushroom.common.annotation.Log;
import com.enokimushroom.common.core.controller.BaseController;
import com.enokimushroom.common.core.domain.AjaxResult;
import com.enokimushroom.common.core.page.TableDataInfo;
import com.enokimushroom.common.enums.BusinessType;
import com.enokimushroom.common.utils.poi.ExcelUtil;
import com.enokimushroom.entity.EnokimushroomCluster;
import com.enokimushroom.service.IEnokimushroomClusterService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产量预期Controller
 * 
 * @author yejainfeng
 * @date 2025-08-18
 */
@RestController
@RequestMapping("/system/starryCluster")
public class EnokimushroomClusterController extends BaseController
{
    @Autowired
    private IEnokimushroomClusterService enokimushroomClusterService;

    /**
     * 查询产量预期列表
     */
//    @PreAuthorize("@ss.hasPermi('system:starryCluster:list')")
    @GetMapping("/list")
    public TableDataInfo list(EnokimushroomCluster enokimushroomCluster)
    {
        startPage();
        List<EnokimushroomCluster> list = enokimushroomClusterService.selectEnokimushroomClusterList(enokimushroomCluster);
        return getDataTable(list);
    }

    /**
     * 导出产量预期列表
     */
    @PreAuthorize("@ss.hasPermi('system:starryCluster:export')")
    @Log(title = "产量预期", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EnokimushroomCluster enokimushroomCluster)
    {
        List<EnokimushroomCluster> list = enokimushroomClusterService.selectEnokimushroomClusterList(enokimushroomCluster);
        ExcelUtil<EnokimushroomCluster> util = new ExcelUtil<EnokimushroomCluster>(EnokimushroomCluster.class);
        util.exportExcel(response, list, "产量预期数据");
    }

    /**
     * 获取产量预期详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:starryCluster:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(enokimushroomClusterService.selectEnokimushroomClusterById(id));
    }

    /**
     * 新增产量预期
     */
//    @PreAuthorize("@ss.hasPermi('system:starryCluster:add')")
    @Log(title = "产量预期", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody EnokimushroomCluster enokimushroomCluster)
    {
        return toAjax(enokimushroomClusterService.insertEnokimushroomCluster(enokimushroomCluster));
    }

    /**
     * 修改产量预期
     */
    @PreAuthorize("@ss.hasPermi('system:starryCluster:edit')")
    @Log(title = "产量预期", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody EnokimushroomCluster enokimushroomCluster)
    {
        return toAjax(enokimushroomClusterService.updateEnokimushroomCluster(enokimushroomCluster));
    }

    /**
     * 删除产量预期
     */
    @PreAuthorize("@ss.hasPermi('system:starryCluster:remove')")
    @Log(title = "产量预期", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(enokimushroomClusterService.deleteEnokimushroomClusterByIds(ids));
    }
}
