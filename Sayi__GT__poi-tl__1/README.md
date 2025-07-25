# Poi-tl(Poi-template-language)

[![Build Status](https://travis-ci.org/Sayi/poi-tl.svg?branch=master)](https://travis-ci.org/Sayi/poi-tl) ![jdk1.6+](https://img.shields.io/badge/jdk-1.6%2B-orange.svg) ![poi3.16](https://img.shields.io/badge/apache--poi-3.16-blue.svg) 

:memo:  Word 模板引擎，基于Apache POI进行了一些增强封装，如合并多个Word文档、合并单元格、图片处理等，插件机制使得可以基于模板引擎特性扩展出更丰富的功能。

> **模板和插件构建了整个Poi-tl的核心。**

下表对一些处理Word的解决方案作了一些比较：

| 方案 | 跨平台 | 样式处理  | 易用性
| --- | --- | --- | --- |
| **Poi-tl** | 纯Java组件，跨平台 | :white_check_mark: 不需要编码，模板即样式 | :white_check_mark: 简单：模板引擎，对POI进行封装
| Apache POI | 纯Java组件，跨平台 | 编码 | 简单，没有模板引擎功能
| Freemarker | XML操作，跨平台 | 无 | 复杂，需要理解XML结构，基于XML构造模板
| OpenOffice | 需要安装OpenOffice软件 | 编码 | 复杂，需要了解OpenOffice的API
| Jacob、winlib | Windows平台 | 编码 | 复杂，不推荐使用

## Maven

```xml
<dependency>
  <groupId>com.deepoove</groupId>
  <artifactId>poi-tl</artifactId>
  <version>1.4.2</version>
</dependency>
```

## 2分钟快速入门
从一个超级简单的例子开始：把{{title}}替换成"Poi-tl 模板引擎"。

1. 新建文档template.docx，包含文本{{title}}
2. TDO模式：Template + data-model = output

```java
//核心API采用了极简设计，只需要一行代码
XWPFTemplate template = XWPFTemplate.compile("~/template.docx").render(new HashMap<String, Object>(){{
        put("title", "Poi-tl 模板引擎");
}});
FileOutputStream out = new FileOutputStream("out_template.docx");
template.write(out);
out.flush();
out.close();
template.close();
```

## 详细文档与示例

[中文文档](http://deepoove.com/poi-tl) or [English-tutorial Wiki](https://github.com/Sayi/poi-tl/wiki/2.English-tutorial)

* [基础(图片、文本、表格、列表)示例：软件说明文档](http://deepoove.com/poi-tl/#_%E8%BD%AF%E4%BB%B6%E8%AF%B4%E6%98%8E%E6%96%87%E6%A1%A3)
* [表格示例：付款通知书](http://deepoove.com/poi-tl/#example-table)
* [循环模板示例：文章写作](http://deepoove.com/poi-tl/#example-article)
* [Example：个人简历](http://deepoove.com/poi-tl/#_%E4%B8%AA%E4%BA%BA%E7%AE%80%E5%8E%86)

更多的示例以及所有示例的源码参见JUnit单元测试。

![](http://deepoove.com/poi-tl/demo.png)
![](http://deepoove.com/poi-tl/demo_result.png)

## 架构Arch
**Poi-tl**通过极简的架构实现了模板功能并且支持最大的扩展性，架包体积仅有几十KB。

整体设计采用了`Template + data-model = output`模式，**Configure**提供了模板配置功能，比如语法配置和插件配置，**Visitor**提供了模板解析功能，**RenderPolicy**是渲染策略扩展点，**Render**模块通过**RenderPolicy**对每个标签进行渲染。

![](http://deepoove.com/poi-tl/arch.png)

## 建议和完善
参见[常见问题](http://deepoove.com/poi-tl/#_%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)，欢迎在GitHub Issue中提问和交流。

