# 数据验证 Validator 

Jboot 从 V3.7.5 开始，增强 Jboot  的验证方式，在 Jboot 之前的 @EmptyValidate、@RegexValidate 等基础上，进一步基于 JSR 303 – Bean Validation 简化了验证方式，相比 Spring 更加优雅简单。

[[toc]]

## @NotNull

在 Controller （或 Service 等）中，我们可以直接通过 @NotNull 注解给 Controller 添加，例如：

```java
@RequestMapping("/")
public class MyController extends JbootController {

    public void test(@NotNull String para) {
        renderText("test6");
    }
}
```

当我们访问 `/test` 的时候，会出现如下的错误：

```
para is null at method: io.jboot.test.MyController.test(java.lang.String) : /test
io.jboot.components.valid.ValidException : 不能为null
        at io.jboot.components.valid.ValidUtil.throwValidException(ValidUtil.java:59)
        at io.jboot.components.valid.ValidUtil.throwValidException(ValidUtil.java:50)
        at io.jboot.components.valid.interceptor.NotNullInterceptor.intercept(NotNullInterceptor.java:36)
```

如果是 ajax （或者 content-type 为 "application/json"） 访问 `/test` 的时候，会返回如下的 json 信息：

```json
{
    "throwable": "io.jboot.components.valid.ValidException: 不能为null",
    "errorMessage": "para is null at method: io.jboot.test.MyController.test(java.lang.String)",
    "errorCode": 400,
    "state": "fail",
    "message": "不能为null"
}
```

如果我们访问 `/test?para=123`，则可以正常访问，不会出错，此时 `test` 方法里的 para 的值为 `123`（不为 null）。

## @Size 验证

@Size 验证，不仅仅可以验证 String 数据的长度，也可以验证 int long 等数据类型的值的大小范围。比如：

```java
public void test(@Size(min=2,max=10) int value) {
    renderText("test6");
}
```

这个要求 value 的值必须在 2 ~ 10 之间。


当然，我们还可以使用 @Size 来验证 `Map`、`List`、`数组` 的长度，比如配合 @JsonBody 来接收前端传入的值：

```java
public void list(@Size(min=2,max=10) @JsonBody() List<MyBean> list) {        
    System.out.println("list--->" + list);        
    renderText("ok");
}
```

要求前度传入的 MyBean Json 数组的数量必须是在 2~10 之间。

> 更多关于 @JsonBody 请参考：[这个链接](./json.md)。

## @NotEmpty 验证

@NotEmpty 不仅仅可以验证 String 类型不能为 null 和 空字符串，也可以验证 Map、List、数组等不能为空，比如：

```java
public void list(@NotEmpty() @JsonBody() List<MyBean> list) {        
    System.out.println("list--->" + list);        
    renderText("ok");
}
```

要求前端掺入的 MyBean Json 数组必须有值。

## @Valid 验证
@Valid 是针对整个 Java Bean 验证，也可以对 JFinal 的 Model 进行验证。的 MyBean Json 数

比如 MyBean 定义如下：

```java
public class MyBean {
    private String id;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Size(min=0,max=2,message = "性别的值只能是 0 1 2")
    private int sex;

    @Min(value = 18,message = "未成年禁止入内")  
    private Integer age; 
}
```

在 Controller 或者 Service 中，如下代码可以直接对 MyBean 进行验证：

```java
public void test(@Valid() MyBean bean) {
    renderText("test6");
}
```

如果 MyBean 是一个 JFinal 的 Model，我们只需要在 getter 方法添加注解即可。

## 更多的验证
除了以上的基本示例以外，Jboot 的验证还支持了更多的验证：

| 注解   | 说明  |
|  ----  | ----  |
| @NotNull  | 	限制必须不为null |
| @DecimalMax(value)  | 	限制必须为一个不大于指定值的数字 |
| @DecimalMin(value)  | 	限制必须为一个不小于指定值的数字 |
| @Digits(integer,fraction)	  | 限制必须为一个小数，且整数部分的位数不能超过integer，小数部分的位数不能超过fraction |
| @Max(value)	  | 限制必须为一个不大于指定值的数字 |
| @Min(value)	  | 限制必须为一个不小于指定值的数字 |
| @Pattern(value)  | 	限制必须符合指定的正则表达式 |
| @Size(max,min)  | 	限制字符长度必须在min到max之间 |
| @NotEmpty  | 	验证注解的元素值不为null且不为空（字符串长度不为0、集合大小不为0） |
| @NotBlank	  | 验证注解的元素值不为空（不为null、去除首位空格后长度为0），不同于@NotEmpty，@NotBlank只应用于字符串且在比较时会去除字符串的空格 |
| @Email  | 	验证注解的元素值是Email，也可以通过正则表达式和flag指定自定义的email格式 |

##  @EmptyValidate 和 @RegexValidate 

当如果我们的 `Controller` 的方法没有参数的时候，就无法使用如上的注解进行验证了。因为，以上的注解
是给方法里的参数进行添加。在这个时候，我们可以使用 `@EmptyValidate` 和 `@RegexValidate` 进行验证。

比如：

```java
@RequestMapping("/")
public class MyController extends JbootController {

    @EmptyValidate(@Form(name = "mobile", message = "手机号不能为空"))
    public void action1() {
        renderText("ok");
    }


    @RegexValidate(@RegexForm(name = "mobile", regex = Regex.MOBILE, message= "您输入的不是手机号"))
    public void action2() {
        renderText("ok");
    }

}
```

- 当我们访问 `/action1` 的时候，会出现手机号不能为空的错误提示。访问 `/action1?mobile=123` 的时候，正常访问。
- 当我们访问 `/action2` 或者 `/action2?mobile=123`  的时候，会出现手机号不正确的错误提示。
  访问 `/action2?mobile=18611223344` 的时候，正常访问。因为 `18611223344` 是一个正确的手机号 。

## 自定义渲染器

   我们可以写一个 `RenderFactory` 继承自 `JbootRenderFactory` ，并复写其 `getValidErrorRender()` 方法。

   例如：

```java
public MyRenderFactory extends JbootRenderFactory(){

    @Override
    public ValidErrorRender getValidErrorRender(ValidException validException){
        //return 返回自己写渲染器
    }
}
```

然后在项目启动的时候，通过在 `onConstantConfig(Constants constants)` 里配置上自己的 `MyRenderFactory`:

```java
@Override
public void onConstantConfig(Constants constants) {
    constants.setRenderFactory(new MyRenderFactory());
}
 
```