package com.blocklang.core.test;

import org.springframework.test.context.ActiveProfiles;

// 测试环境下，支持两个 profile，分别为 test 和 mysql
// 值为 test 时，默认在 postgresql 数据库上运行测试
// 值为 mysql 时，在 mysql 数据库上运行测试
@ActiveProfiles("test")
public abstract class AbstractSpringTest {

}
