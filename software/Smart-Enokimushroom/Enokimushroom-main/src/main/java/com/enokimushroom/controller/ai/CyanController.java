package com.enokimushroom.controller.ai;

import com.enokimushroom.common.annotation.Log;
import com.enokimushroom.common.core.controller.BaseController;
import com.enokimushroom.common.core.domain.AjaxResult;
import com.enokimushroom.common.core.page.TableDataInfo;
import com.enokimushroom.common.enums.BusinessType;
import com.enokimushroom.common.utils.poi.ExcelUtil;

import com.enokimushroom.entity.EnokimushroomPestDisease;
import com.enokimushroom.service.IEnokimushroomPestDiseaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CyanController
{
    @Autowired
    private IEnokimushroomPestDiseaseService enokimushroomPestDiseaseService;

    @GetMapping("/cyan")
    public TableDataInfo list(EnokimushroomPestDisease enokimushroomPestDisease)
    {
        System.out.println("设备启动成功");
        return null;
    }
}
