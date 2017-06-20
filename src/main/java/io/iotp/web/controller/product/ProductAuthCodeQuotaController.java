/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-02-08
 */
package io.iotp.web.controller.product;

import io.iotp.authcode.dto.ProductAuthCodeDto;
import io.iotp.authcode.dto.ProductAuthCodeQuotaDto;
import io.iotp.authcode.service.ProductAuthCodeQuotaService;
import io.iotp.lease.product.dto.LeaseProductDto;
import io.iotp.lease.product.service.LeaseProductService;
import io.iotp.module.authcode.entity.ProductAuthCode;
import io.iotp.module.authcode.entity.ProductAuthCodeQuota;
import io.iotp.module.lease.product.entity.LeaseProduct;
import io.iotp.module.product.entity.Product;
import io.iotp.module.product.entity.ProductSpec;
import io.iotp.product.dto.ProductDto;
import io.iotp.product.service.ProductService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 产品授权配额管理
 *
 * @author CD826
 * @since 2.0.0
 */
@Controller
@RequestMapping("/shop/product/authCode")
@UserLogConfig(moduleName = "产品授权配额管理-ProductAuthCodeQuotaController")
public class ProductAuthCodeQuotaController extends IotpCrudController<ProductAuthCodeQuota, ProductAuthCodeQuotaDto, ProductAuthCodeQuotaService> {

    @Autowired
    private ProductService productService;

    @Autowired
    private LeaseProductService leaseProductService;

    @Override
    protected void onList() {
        super.onList();
        this.forwardParameters("type");
    }

    /**
     * 页签列表页面
     *
     * @return 页面路径
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public String list4tab(){
        this.forwardParameters("productId", "type", "leaseProductId");
        return this.viewPath("/list4product");
    }

    @Override
    public Page<ProductAuthCodeQuotaDto> getPageData(@PageableDefault(sort = {"created"}, direction = Sort.Direction.DESC) Pageable pageable, String searchPhrase) {
        Page<ProductAuthCodeQuota> page;
        String type = this.getParameter("type", "product");
        if (type.equalsIgnoreCase("product")) {
            // 普通产品授权
            String productId = this.getParameter("productId", "");
            if(StringUtils.hasText(productId)){
                page = this.service.getPage(pageable, getCurShop().getId(), IdConfuseUtils.decodeId(productId), searchPhrase);
            } else {
                page = this.service.getPage(pageable, getCurShop().getId(), searchPhrase);
            }
        }else{
            // 租赁产品授权
            String leaseProductId = this.getParameter("leaseProductId", "");
            if(StringUtils.hasText(leaseProductId)){
                page = this.service.getLeaseProductAuthPage(pageable, getCurShop().getId(), IdConfuseUtils.decodeId(leaseProductId));
            } else {
                page = this.service.getLeaseProductAuthPage(pageable, getCurShop().getId(), searchPhrase);
            }
        }
        return this.mapToDtoPage(pageable, page);
    }

    @Override
    protected void onOpenCreateForm(Model model, ProductAuthCodeQuotaDto dto) {
        super.onOpenCreateForm(model, dto);

        String type = this.getParameter("type", "product");
        this.forwardParameters("type");

        if (type.equalsIgnoreCase("product")) {
            // 产品信息
            String productId = this.getParameter("productId", "");
            if(StringUtils.hasText(productId)){
                Product product = this.productService.load(IdConfuseUtils.decodeId(productId));
                if (null != product) {
                    model.addAttribute("product", this.entityDtoMapService.mapToDto(product, ProductDto.class));
                    if(!product.isMultiSpec()){
                        dto.setItemCode(product.getItemCode());
                    }
                    dto.setProductId(productId);
                }
            }
            dto.setAuthType(ProductAuthCodeQuota.AT_COMMOM);
        }else{
            // 租赁产品信息
            String leaseProductId = this.getParameter("leaseProductId", "");
            if(StringUtils.hasText(leaseProductId)){
                LeaseProduct leaseProduct = this.leaseProductService.load(IdConfuseUtils.decodeId(leaseProductId));
                // 获取所属产品itemCode
                if (null != leaseProduct) {
                    dto.setProductId(IdConfuseUtils.encodeId(leaseProduct.getProductId()));

                    if (null != leaseProduct.getProductSpecId() && leaseProduct.getProductSpecId() > 0) {
                        ProductSpec productSpec = this.productService.loadProductSpec(leaseProduct.getProductSpecId());
                        if(null != productSpec)
                            dto.setItemCode(productSpec.getItemCode());
                    } else {
                        if (null != leaseProduct.getProductId()) {
                            Product product = this.productService.load(leaseProduct.getProductId());
                            if(null != product)
                                dto.setItemCode(product.getItemCode());
                        }
                    }
                }
                model.addAttribute("leaseProduct", this.entityDtoMapService.mapToDto(leaseProduct, LeaseProductDto.class));
            }
            dto.setAuthType(ProductAuthCodeQuota.AT_LEASE);
        }

        // 授权起始码
        long authCodeStart;
        ProductAuthCodeQuota authCodeQuota = this.service.findLastOne(getCurShop().getId());
        if(null != authCodeQuota){
            authCodeStart = authCodeQuota.getAuthCodeEnd() + 1;
        } else {
            authCodeStart = getCurShop().getId() * 1000000;
        }
        dto.setAuthCodeStart((int)authCodeStart);
    }

    @Override
    protected String createFormPath() {
        // 对话框方式创建
        return this.viewPath("/createDlg");
    }

    @Override
    protected void onSaveEntity(ProductAuthCodeQuota entity) {
        super.onSaveEntity(entity);

        entity.setShopId(getCurShop().getId());
        if(entity.isNew())
            entity.setCreatedAt(new Date());

        // 产品id
        String productConfuseId = this.getParameter("productConfuseId", "");
        if(StringUtils.hasText(productConfuseId))
            entity.setProductId(IdConfuseUtils.decodeId(productConfuseId));

        // 产品规格id
        String productSpecConfuseId = this.getParameter("productSpecConfuseId", "");
        if(StringUtils.hasText(productSpecConfuseId))
            entity.setProductSpecId(IdConfuseUtils.decodeId(productSpecConfuseId));

        // 租赁产品id
        String leaseProductConfuseId = this.getParameter("leaseProductConfuseId", "");
        if(StringUtils.hasText(leaseProductConfuseId))
            entity.setLeaseProductId(IdConfuseUtils.decodeId(leaseProductConfuseId));
    }



    // ==================================================================
    // 使用记录 ===========================================================
    /**
     * 查看使用记录对话框
     *
     * @param model     model
     * @param confuseId 授权id
     * @return 对话框页面
     */
    @RequestMapping(value = "/{confuseId}/viewUsed", method = RequestMethod.GET)
    public String viewUsed(Model model, @PathVariable String confuseId){
        model.addAttribute("productAuthCodeQuotaId", confuseId);
        return this.viewPath("/usedDlg::usedDlg");
    }

    /**
     * 获取使用记录分页
     *
     * @param pageable  分页数据
     * @param confuseId 所属授权信息id
     * @return
     */
    @RequestMapping(value = "/{confuseId}/viewUsed/page", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getProductQRCodeUsedPage(Pageable pageable, @PathVariable String confuseId){
        Pageable newPageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(Sort.Direction.DESC, "usedAt"));
        Page<ProductAuthCode> page = this.service.getProductAuthCodePage(newPageable, IdConfuseUtils.decodeId(confuseId));
        Page<ProductAuthCodeDto> dtoPage = this.entityDtoMapService.mapToDtoPage(newPageable, page, ProductAuthCodeDto.class);

        ResponseData responseData = new ResponseData();
        responseData.put("current", pageable.getPageNumber() + 1);
        responseData.put("rowCount", pageable.getPageSize());
        responseData.put("total", dtoPage.getTotalElements());
        responseData.put("rows", dtoPage.getContent());
        return responseData;
    }

    /**
     * 转到打印二维码页面
     *
     * @return
     */
    @RequestMapping(value = "/{confuseId}/qrcode/toPrint", method = RequestMethod.GET)
    public String toPrintQRCode(Model model, @PathVariable String confuseId){
        ProductAuthCodeQuota quota = this.service.load(IdConfuseUtils.decodeId(confuseId));
        if(null != quota){
            model.addAttribute(ENTITY_NAME, this.mapToDto(quota));
        }
        model.addAttribute("size", 200);
        this.forwardParameters("type");
        return this.viewPath("/printQRCodeDlg");
    }

    /**
     * 绘制二维码
     *
     * @return
     */
    @RequestMapping(value = "/{confuseId}/qrcode/doPrint", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData doPrintQRCode(@PathVariable String confuseId, @RequestParam(defaultValue = "200") int size){
        ResponseData responseData = new ResponseData();
        try{
            String folder = this.service.encoderQRCode(getCurUser(), IdConfuseUtils.decodeId(confuseId), size);
            responseData.put("folder", folder);
        }catch (Exception e){
            responseData = new ResponseData(9, "二维码图片打印失败");
        }
        return responseData;
    }

    /**
     * 跳转到等待下载二维码页面
     *
     * @throws Exception
     */
    @RequestMapping(value = "qrcode/predownload", method = RequestMethod.GET)
    public String predownLoadQrCodes() throws Exception {
        return this.viewPath("/predownload");
    }

    /**
     * 打印并下载二维码
     *
     * @param confuseId  授权信息id
     * @param qrcodePath 二维码图片文件夹路径
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{confuseId}/qrcode/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downLoadQrCodes(@PathVariable String confuseId, @RequestParam String qrcodePath) throws Exception {
        try{
            File zipFile = this.zipQRCodes(qrcodePath);
            if(null == zipFile)
                throw new Exception("二维码图片文件压缩失败");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(zipFile.getName(), "UTF-8"));
            return new ResponseEntity<>(FileUtils.readFileToByteArray(zipFile), headers, HttpStatus.CREATED);
        } catch (IOException ex) {
            throw new Exception(ex);
        }
    }


    // ==========================================================================
    // private methods ==========================================================

    /**
     * 压缩二维码文件夹目录
     *
     * @param folder     二维码文件夹目录
     * @return 最终压缩文件
     * @throws Exception
     */
    private File zipQRCodes(String folder) throws Exception {
        ZipOutputStream zipOutputStream = null;
        File zipFile = null;

        try{
            File qrcodeFolder = new File(folder);
            if(!qrcodeFolder.exists())
                throw new Exception("二维码目录不存在");

            String[] subFiles = qrcodeFolder.list();
            if(null == subFiles || subFiles.length == 0)
                throw new Exception("未找到任何二维码图片");

            // 压缩文件
            zipFile = new File(folder + ".zip");
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            // 遍历子文件并压缩
            FileInputStream inputStream = null;
            byte[] buffer = new byte[1024];
            for (String file : subFiles){
                String fileEntryName = folder + File.separator + file;
                zipOutputStream.putNextEntry(new ZipEntry(fileEntryName));

                try{
                    inputStream = new FileInputStream(fileEntryName);
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0, len);
                    }
                }finally {
                    assert inputStream != null;
                    inputStream.close();
                }
            }
        }catch (Exception e){
            throw new Exception(e);
        }finally {
            assert zipOutputStream != null;
            zipOutputStream.close();
        }
        return zipFile;
    }
}
