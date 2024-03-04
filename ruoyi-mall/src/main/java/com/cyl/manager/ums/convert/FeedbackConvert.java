package com.cyl.manager.ums.convert;

import org.mapstruct.Mapper;
import com.cyl.manager.ums.domain.Feedback;
import com.cyl.manager.ums.pojo.vo.FeedbackVO;
import java.util.List;
/**
 * 意见反馈  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface FeedbackConvert  {

    List<FeedbackVO> dos2vos(List<Feedback> list);
}