package com.wulee.administrator.zuji.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * create by  wulee   2018/11/13 17:45
 * desc: 用户分组item（根据注册时间分组）
 */
public class UserGroupItem implements MultiItemEntity {
    //类型--内容
    public static final int ITEM  = 0;
    //类型--顶部悬浮的标题
    public static final int SECTION = 1;

    public  int mItemType; //所属于的类型

    public final PersonInfo mPersonInfo; //listview显示的item的数据实体类


    public UserGroupItem(int itemType, PersonInfo personInfo) {
        super();
        this.mItemType =  itemType;
        this.mPersonInfo = personInfo;
    }

    /**
     * 通过HashMap键值对的特性，将ArrayList的数据进行分组，返回带有分组Header的ArrayList。
     * @param originalList 从后台接受到的ArrayList的数据
     * @return list  返回的list是分类后的包含header（登记人名称-职称-科室）和item（洗手特征  登记时间）的ArrayList
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ArrayList<UserGroupItem> getData( List<PersonInfo> originalList) {
        //最后我们要返回带有分组的list,初始化
        ArrayList<UserGroupItem> list = new ArrayList<>();
        /*2、分组算法**/
        Map<String, List<PersonInfo>> map = new HashMap<>();
        for (PersonInfo personInfo : originalList) {
            List<PersonInfo> tempList = map.get(personInfo.buildGroupKey());
            /*如果取不到数据,那么直接new一个空的ArrayList**/
            if (tempList == null) {
                tempList = new ArrayList<>();
                tempList.add(personInfo);
                map.put(personInfo.buildGroupKey(), tempList);
            }
            else {
                /*某个dependence之前已经存放过了,则直接追加数据到原来的List里**/
                tempList.add(personInfo);
            }
        }

        // 用迭代器遍历map添加到list里面
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            //key作为标题.类别属于SECTION
            PersonInfo entity = new PersonInfo();
            entity.setGroupKey(key);
            list.add(new UserGroupItem(SECTION, entity));
            List<PersonInfo> entityList = (List<PersonInfo>) entry.getValue();
            for (PersonInfo personInfo : entityList) {
                //对应的作为标题下的item,类别属于ITEM
                list.add(new UserGroupItem(ITEM, personInfo));
            }
        }
        // 把分好类的hashmap添加到list里面便于显示
        return list;
    }

    @Override
    public int getItemType() {
        return mItemType;
    }
}
