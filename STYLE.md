# 编码规范

## Java

1. 方法返回类型为 List 或者数组时，当没有数据时，不能返回 null，而应返回空列表或空数组。
2. 测试方法的命名，以被测的函数名开头，后面用下划线分割的方式命名，这样容易根据方法名搜索。
   
   ```java
    // findProjectBuildDependences_no_dependence 是方法名
    // _no_dependence 是对测试用例的简单描述
    @DisplayName("find project's build dependences: has no dependences")
    @Test
	public void findProjectBuildDependences_no_dependence() { }
   ```

## TypeScript

函数或方法返回的类型为数组时，当没有数据时，不能返回 undefined，而应返回空数组。
