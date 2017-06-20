/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-04-12
 */
package io.iotp.api.web.lease;

import com.xiaochenghudong.core.account.entity.User;
import io.iotp.api.service.DeviceService;
import io.iotp.core.util.ReportDateScopeType;
import io.iotp.iot.dto.WaterDeviceDataNodeDto;
import io.iotp.iot.dto.WaterDevicePartDto;
import io.iotp.iot.dto.WaterDeviceReportData;
import io.iotp.lease.dto.LeaseDeviceDashboard;
import io.iotp.lease.dto.LeaseDeviceDto;
import io.iotp.lease.dto.RechargeOrderDto;
import io.iotp.lease.service.LeaseDeviceService;
import io.iotp.module.device.entity.Device;
import io.iotp.module.serviceorder.entity.ServiceOrder;
import io.iotp.serviceorder.dto.ServiceOrderDto;
import io.iotp.serviceorder.service.ServiceOrderService;
import io.iotp.api.IotpEndpoint;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiPage;
import io.springbootstrap.core.api.ApiResponseData;
import io.springbootstrap.core.util.DateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * FIXME：这里需要增加安全注解
 *
 * 租赁设备管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/lease/devices")
@UserLogConfig(moduleName = "租赁设备管理")
@Api(value = "LeaseDeviceEndpoint", description = "租赁管理相关Api")
public class LeaseDeviceEndpoint extends IotpEndpoint<Device, LeaseDeviceDto> {
    // ========================================================================
    // constants ==============================================================
    /** 视图类别: 离线设备, 值为:{@value} */
    private static final String VT_OFFLINE      				    = "offline";
    /** 视图类别: 异常设备, 值为:{@value} */
    private static final String VT_ERROR      				        = "error";
    /** 视图类别: 即将到期设备, 值为:{@value} */
    private static final String VT_DUESOON     				        = "soon";
    /** 视图类别: 逾期设备, 值为:{@value} */
    private static final String VT_OVERDUE     				        = "overdue";

    @Autowired
    private LeaseDeviceService leaseDeviceService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ServiceOrderService serviceOrderService;

    /**
     * 获取租赁设备统计信息
     * @return
     */
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    @ApiOperation(value = "获取租赁设备统计信息", notes = "获取租赁设备统计信息", httpMethod = "GET", tags = "Web端API -- 租赁设备管理")
    @UserLogable(methodName = "获取租赁设备统计信息")
    public ApiResponseData<LeaseDeviceDashboard> dashboard() {
        return this.wrap(this.leaseDeviceService.dashboard(this.getCurUser(), this.getCurShopId()));
    }

    /**
     * 获取租赁设备的分页数据
     * @param pageable
     * @param searchPhrase
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "租赁设备分页数据", notes = "租赁设备分页数据", httpMethod = "GET", tags = "Web端API -- 租赁设备管理")
    @UserLogable(methodName = "租赁设备分页数据")
    public ApiResponseData<ApiPage<LeaseDeviceDto>> list(@ApiParam(value = "分页参数", required = true) Pageable pageable,
                                                         @ApiParam(value = "视图类型", required = false) @RequestParam(required = false) String viewType,
                                                         @ApiParam(value = "租赁产品", required = false) @RequestParam(required = false) String leaseProductId,
                                                         @ApiParam(value = "安装地址_省", required = false) @RequestParam(required = false) String province,
                                                         @ApiParam(value = "安装地址_市", required = false) @RequestParam(required = false) String city,
                                                         @ApiParam(value = "安装地址_区", required = false) @RequestParam(required = false) String area,
                                                         @ApiParam(value = "查询参数", required = false) @RequestParam(required = false) String searchPhrase) {
        User curUser = this.getCurUser();
        Long shopId = this.getCurShopId();
        Long leaseProduct = this.decodeEntityId(leaseProductId);

        Page<LeaseDeviceDto> page = null;
        if (VT_OFFLINE.equalsIgnoreCase(viewType)) {
            // 离线设备
            page = this.leaseDeviceService.getOfflinePage(pageable, curUser, shopId, leaseProduct, province, city, area, searchPhrase);
        } else if (VT_ERROR.equalsIgnoreCase(viewType)) {
            // 异常设备
            page = this.leaseDeviceService.getErrorPage(pageable, curUser, shopId, leaseProduct, province, city, area, searchPhrase);
        } else if (VT_DUESOON.equalsIgnoreCase(viewType)) {
            // 即将到期
            page = this.leaseDeviceService.getPage(pageable, curUser, shopId, leaseProduct, new Date(), this.leaseDeviceService.getDuesoonDate(), province, city, area, searchPhrase);
        } else if (VT_OVERDUE.equalsIgnoreCase(viewType)) {
            // 已逾期
            page = this.leaseDeviceService.getOverduePage(pageable, curUser, shopId, leaseProduct, province, city, area, searchPhrase);
        } else {
            // 所有设备
            page = this.leaseDeviceService.getPage(pageable, curUser, shopId, leaseProduct, null, null, province, city, area, searchPhrase);
        }
        return this.wrapPage(page);
    }

    /**
     * 获取指定租赁设备的详细信息
     * @param entityId 租赁设备的Id(混淆后的)
     * @return 租赁设备详细信息数据
     */
    @RequestMapping(value= "/{entityId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定租赁设备的详细数据", notes = "获取指定租赁设备的详细数据", httpMethod = "GET", tags = "Web端API -- 租赁设备管理")
    public ApiResponseData<LeaseDeviceDto> detail(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId) {
        LeaseDeviceDto leaseDeviceDto = this.leaseDeviceService.load(this.getCurUser(), this.decodeEntityId(entityId));
        if(null == leaseDeviceDto) {
            // FIXME: 错误体系
            return new ApiResponseData<>(40001, "租赁设备不存在");
        } else {
            return this.wrap(leaseDeviceDto);
        }
    }

    /**
     * 获取指定租赁设备的充值记录
     * @param entityId 租赁设备的Id(混淆后的)
     * @return 相应充值记录列表数据
     */
    @RequestMapping(value= "/{entityId}/rechargeOrders", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定租赁设备的充值记录", notes = "获取指定租赁设备的充值记录", httpMethod = "GET", tags = "Web端API -- 租赁设备管理")
    public ApiResponseData<List<RechargeOrderDto>> rechargeOrders(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId) {
        return this.wrap(this.leaseDeviceService.findAllRechargeOrders(this.getCurUser(), this.decodeEntityId(entityId)));
    }

    /**
     * 获取指定租赁设备的服务记录
     * @param entityId 租赁设备的Id(混淆后的)
     * @return 相应服务记录列表数据
     */
    @RequestMapping(value= "/{entityId}/serviceOrders", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定租赁设备的服务记录", notes = "获取指定租赁设备的服务记录", httpMethod = "GET", tags = "Web端API -- 租赁设备管理")
    public ApiResponseData<List<ServiceOrderDto>> serviceOrders(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId) {
        return this.wrap(this.serviceOrderService.findAllByDevice(this.decodeEntityId(entityId), ServiceOrder.OT_COMMOM));
    }

    /**
     * 获取指定租赁设备的净水明细
     * @param entityId 租赁设备的Id(混淆后的)
     * @return 相应净水记录分页数据
     */
    @RequestMapping(value= "/{entityId}/datanodes", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定租赁设备的净水明细", notes = "获取指定租赁设备的净水明细", httpMethod = "GET", tags = "Web端API -- 租赁设备管理")
    public ApiResponseData<ApiPage<WaterDeviceDataNodeDto>> datanodes(@ApiParam(value = "分页参数", required = true) @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                      @PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId,
                                                                      @ApiParam(value = "起始日期(yyyy-MM-dd)") String startDate) {
        Date start = DateUtils.parseDate(startDate);
        return this.wrapPage(this.leaseDeviceService.getDatanodePage(pageable, this.getCurUser(), this.decodeEntityId(entityId), start, null));
    }

    /**
     * 获取指定租赁设备的统计报表数据
     * @param entityId 租赁设备的Id(混淆后的)
     * @return 相应统计报表数据净水记录分页数据
     */
    @RequestMapping(value= "/{entityId}/report", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定租赁设备的统计报表数据", notes = "获取指定租赁设备的统计报表数据", httpMethod = "GET", tags = "Web端API -- 租赁设备管理")
    public ApiResponseData<List<WaterDeviceReportData>> reportData(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId,
                                                                   @ApiParam(value = "统计报表数据日期范围(w/m/y)") String dateScopeType,
                                                                   @ApiParam(value = "统计报表起始日期(yyyy-MM-dd)") String startDate) {
        ReportDateScopeType scopeType = ReportDateScopeType.WEEK;
        if ("m".equals(dateScopeType))
            scopeType = ReportDateScopeType.MONTH;
        if ("y".equals(dateScopeType))
            scopeType = ReportDateScopeType.YEAR;

        Date start = DateUtils.parseDate(startDate);
        return this.wrap(this.leaseDeviceService.getReportData(this.getCurUser(), this.decodeEntityId(entityId), scopeType, start));
    }

    /**
     * 重置滤芯
     * @param entityId 租赁设备的Id(混淆后的)
     * @param filterIndex 滤芯的顺序号
     * @return 租赁设备详细信息数据
     */
    @RequestMapping(value= "/{entityId}/resetFilter/{filterIndex}", method = RequestMethod.POST)
    @ApiOperation(value = "重置滤芯", notes = "重置滤芯", httpMethod = "POST", tags = "Web端API -- 租赁设备管理")
    @UserLogable(methodName = "重置滤芯", description = "'设备为: ' + #entityId + '，滤芯为：' + #filterCode")
    public ApiResponseData<WaterDevicePartDto> resetFilter(@PathVariable @ApiParam(value = "Iot设备的Id", required = true) String entityId,
                                                           @PathVariable @ApiParam(value = "所要重置的滤芯的顺序号", required = true) int filterIndex) {
        return this.wrap(this.deviceService.resetFilter(this.getCurUser(), this.decodeEntityId(entityId), filterIndex));
    }

    /**
     * 锁定设备
     * @param entityId 租赁设备的Id(混淆后的)
     * @return 锁定设备时执行的指令Id，后续根据该指令判断是否执行成功
     */
    @RequestMapping(value= "/{entityId}/lock", method = RequestMethod.POST)
    @ApiOperation(value = "锁定设备", notes = "锁定设备", httpMethod = "POST", tags = "Web端API -- 租赁设备管理")
    @UserLogable(methodName = "锁定设备", description = "'设备为: ' + #entityId")
    public ApiResponseData<String> lock(@PathVariable @ApiParam(value = "Iot设备的Id", required = true) String entityId) {
        return this.wrap("");
        // FIXME: return this.wrap(this.leaseDeviceService.lockDevice(this.getCurUser(), this.decodeEntityId(entityId)));
    }

    /**
     * 解锁设备
     * @param entityId 租赁设备的Id(混淆后的)
     * @return 解锁设备时执行的指令Id，后续根据该指令判断是否执行成功
     */
    @RequestMapping(value= "/{entityId}/unlock", method = RequestMethod.POST)
    @ApiOperation(value = "解锁设备", notes = "解锁设备", httpMethod = "POST", tags = "Web端API -- 租赁设备管理")
    @UserLogable(methodName = "解锁设备", description = "'设备为: ' + #entityId")
    public ApiResponseData<String> unlock(@PathVariable @ApiParam(value = "Iot设备的Id", required = true) String entityId) {
        return this.wrap("");
        // FIXME: return this.wrap(this.leaseDeviceService.unlockDevice(this.getCurUser(), this.decodeEntityId(entityId)));
    }

    /**
     * 执行指令
     * @param entityId Iot设备的Id(混淆后的)
     * @param instructionCode 滤芯的编码
     * @return 租赁设备详细信息数据
     *
     * @see io.iotp.module.iot.water.instruction.PowerInstruction#INSTRUCTION_CODE
     * @see io.iotp.module.iot.water.instruction.FlushInstruction#INSTRUCTION_CODE
     * @see io.iotp.module.iot.water.instruction.RestoreDefaultSettingInstruction#INSTRUCTION_CODE
     * @see io.iotp.module.iot.water.instruction.BuzzerTestInstruction#INSTRUCTION_CODE
     * @see io.iotp.module.iot.water.instruction.WaterPumpTestInstruction#INSTRUCTION_CODE
     * @see io.iotp.module.iot.water.instruction.OutValveTestInstruction#INSTRUCTION_CODE
     * @see io.iotp.module.iot.water.instruction.HighVoltageSwitchTestInstruction#INSTRUCTION_CODE
     * @see io.iotp.module.iot.water.instruction.RestoreWiFiInstruction#INSTRUCTION_CODE
     */
    @RequestMapping(value= "/{entityId}/executeInstruction/{instructionCode}", method = RequestMethod.POST)
    @ApiOperation(value = "执行指令", notes = "执行指令", httpMethod = "POST", tags = "Web端API -- 租赁设备管理")
    @UserLogable(methodName = "执行指令", description = "'设备为:' + #entityId + ', 指令为:' + #instructionCode")
    public ApiResponseData<String> executeInstruction(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId,
                                                      @PathVariable @ApiParam(value = "指令代码", required = true) String instructionCode) {
        return this.wrap(this.leaseDeviceService.executeInstruction(this.getCurUser(), this.decodeEntityId(entityId), instructionCode));
    }
}
