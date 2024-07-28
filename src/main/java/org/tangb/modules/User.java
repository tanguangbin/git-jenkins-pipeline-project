package org.tangb.modules;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
//@TableName("system_users")
@TableName("test_user")
public class User {
    @TableId(type=IdType.AUTO)
    private int id;
    private String username;
    private String nickname;
    private int age;
}
