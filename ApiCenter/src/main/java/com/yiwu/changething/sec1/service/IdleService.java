package com.yiwu.changething.sec1.service;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.yiwu.changething.sec1.bean.ShareBean;
import com.yiwu.changething.sec1.enums.IdleOrder;
import com.yiwu.changething.sec1.enums.OrderType;
import com.yiwu.changething.sec1.exception.ErrorBuilder;
import com.yiwu.changething.sec1.exception.YwException;
import com.yiwu.changething.sec1.mapper.IdleMapper;
import com.yiwu.changething.sec1.bean.IdleBean;
import com.yiwu.changething.sec1.mapper.ShareMapper;
import com.yiwu.changething.sec1.model.IdleModel;
import com.yiwu.changething.sec1.model.IdleResModel;
import com.yiwu.changething.sec1.model.ShareModel;
import com.yiwu.changething.sec1.utils.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

/**
 * Created by LinZhongtai <linzhongtai@gengee.cn>
 */
@Service
public class IdleService {

    @Autowired
    private IdleMapper idleMapper;

    @Autowired
    private ShareMapper shareMapper;

    /**
     * 获取商品列表
     *
     * @param idleResModel
     * @return
     */
    public List<IdleBean> getIdleList(IdleResModel idleResModel) {

        if (idleResModel.getPage() != null && idleResModel.getPageSize() != null) {
            idleResModel.setPage((idleResModel.getPage() - 1) * idleResModel.getPageSize());
        }
        return idleMapper.getIdleList(idleResModel);
    }

    /**
     * 获取商品列表总数
     *
     * @param idleResModel
     * @return
     */
    public Integer getIdleCount(IdleResModel idleResModel) {
        return idleMapper.getIdleCount(idleResModel);
    }

    /**
     * 新增商品信息
     *
     * @param idleModel
     */
    public void insert(IdleModel idleModel) {
        idleModel.setId(UUID.randomUUID().toString());
        idleMapper.insert(idleModel);
    }

    /**
     * 修改商品信息
     *
     * @param idleModel
     */
    public void update(IdleModel idleModel) {
        checkIdleExist(idleModel.getId());
        idleMapper.update(idleModel);
    }

    /**
     * 根据id删除商品信息
     *
     * @param idleId
     */
    public void deleteById(String idleId) {
        idleMapper.deleteById(idleId);
    }

    /**
     * 根据id获取商品信息
     *
     * @param idleId
     * @return
     */
    public IdleBean getIdleById(String idleId) {
        IdleBean idleModel = idleMapper.getIdleById(idleId);
        if (idleModel == null) {
            throw new YwException(ErrorBuilder.E101007);
        }
        return idleModel;
    }

    /**
     * 验证商品是否存在
     *
     * @param idleId
     */
    private void checkIdleExist(String idleId) {
        IdleBean idleModel = idleMapper.getIdleById(idleId);
        if (idleModel == null) {
            throw new YwException(ErrorBuilder.E101007);
        }
    }

    /**
     * 更新商品共享状态以及共享值、共享周期
     *
     * @param share
     * @param shareValue
     * @param idleId
     * @param shareCycle
     */
    public void updateShare(Boolean share, Integer shareValue, String idleId, Integer shareCycle,
                            HttpServletRequest request) {
        checkIdleExist(idleId);
        if (!share) {
            shareValue = 0;
        }
        idleMapper.updateShare(share, shareValue, idleId, shareCycle);
        if (share) {
            //将商品上传共享圈子
            insertShare(idleId, shareValue, shareCycle, request);
        }
    }

    /**
     * 上传共享圈子
     *
     * @param idleId
     * @param shareValue
     * @param shareCycle
     * @param request
     */
    public void insertShare(String idleId, Integer shareValue, Integer shareCycle, HttpServletRequest request) {
        ShareBean shareModel = shareMapper.getShareByIdleId(idleId);
        HttpSession session = request.getSession();
        Principal user = (Principal) session.getAttribute(SystemVariableService.USER_INFO);
        if (shareModel == null) {
            ShareModel share = new ShareModel();
            share.setIdleId(idleId);
            share.setUserId(user.getId());
            share.setCycle(shareCycle);
            share.setValue(shareValue);
            shareMapper.insert(share);
        }
        /*else {
            ShareModel updateShare = new ShareModel();
            updateShare.setNum(shareModel.getNum() + 1);//商品共享次数加一
            updateShare.setId(shareModel.getId());
            shareMapper.update(updateShare);
        }*/
    }
}
