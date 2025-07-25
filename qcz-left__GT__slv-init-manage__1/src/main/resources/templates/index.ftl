<!DOCTYPE html>
<html lang="en">
<#include "./include/include.ftl">
<title>Qu管理平台-首页</title>
<style>
    #main-tab-menu table {
        margin: unset;
        background: #e5e5e5;
    }

    .hide {
        display: none!important;
    }
</style>
<body class="layui-layout-body">
<div class="layui-layout layui-layout-admin">
    <div class="layui-header">
        <div class="layui-logo">Qu管理平台</div>
        <ul class="layui-nav layui-layout-right" lay-filter="personal">
            <img id="userImg" src="${ctx}${currentUser.photoPath!'/static/common/image/charon.png'}" class="layui-nav-img" layadmin-event>
            <li class="layui-nav-item">
                <a href="javascript:void(0);">
                    <span id="currentUsername">${currentUser.username}</span>
                </a>
                <dl class="layui-nav-child">
                    <dd>
                        <a href="javascript:void(0);" lay-id="personal-basic-info" lay-href="${ctx}/user/personalBasicInfoPage">基本资料</a>
                    </dd>
                    <dd>
                        <a href="javascript:void(0);" lay-id="safe-setting" lay-href="${ctx}/user/safeSettingPage">安全设置</a>
                    </dd>
                </dl>
            </li>
            <li class="layui-nav-item">
                <a href="javascript:void(0);" class="layui-icon layui-icon-notice" lay-id="message" lay-href="${ctx}/system/message/messageListPage" lay-title="系统通知">
                    <span id="messageCount" style="left: 30px;" class="layui-badge<#if messageCount.all == 0> hide</#if>">${messageCount.all}</span>
                </a>
            </li>
            <li class="layui-nav-item"><a href="${ctx}/logout">退出</a></li>
            <li class="layui-nav-item">
                <div id="choose-color"></div>
            </li>
        </ul>
    </div>
    <div class="layui-side layui-bg-black">
        <div class="layui-side-scroll">
            <!-- 左侧导航区域（可配合layui已有的垂直导航） -->
            <ul class="layui-nav layui-nav-tree" lay-filter="menu-tree">
                <#include '/include/menuInclude.ftl'>
            </ul>
        </div>
    </div>
    <!-- 页面标签 -->
    <div class="layui-body">
        <div class="layui-tab layui-tab-brief" lay-filter="main-tab" lay-stope="tabmore" lay-allowclose="true">
            <div id="main-tab-header">
                <i id="collapseIcon" class="layui-icon layui-icon-shrink-right"></i>
                <i id="expandIcon" class="layui-icon layui-icon-spread-left" style="display: none"></i>
            </div>
            <ul id="main-tab-title" class="layui-tab-title">
            </ul>
            <div class="layui-tab-content">
            </div>
        </div>
        <div id="main-tab-menu" style="display: none;">
            <table class="layui-table">
                <tr>
                    <td class="context-menu-item layui-btn layui-btn-primary" id="closeCurrent">关闭当前</td>
                </tr>
                <tr>
                    <td class="context-menu-item layui-btn layui-btn-primary" id="closeOther">关闭其它</td>
                </tr>
                <tr>
                    <td class="context-menu-item layui-btn layui-btn-primary" id="closeLeft">关闭左侧</td>
                </tr>
                <tr>
                    <td class="context-menu-item layui-btn layui-btn-primary" id="closeRight">关闭右侧</td>
                </tr>
                <tr>
                    <td class="context-menu-item layui-btn layui-btn-primary" id="closeAll">关闭所有</td>
                </tr>
            </table>
        </div>
    </div>

</div>
<link>
<link rel="stylesheet" href="${ctx}/static/css/index.css" />
<script>
top.rsaPublicKey = "${rsaPublicKey!}";
layui.use(['element'], function () {
    let element = layui.element;
    let tabLayFilter = 'main-tab';
    let classLayuiThis = 'layui-this';
    // 监听菜单导航点击
    element.on('nav(menu-tree)', function (elem) {
        let layHref = elem.attr("lay-href");
        if ($("#expandIcon").is(":visible") && !layHref) {
            $("#expandIcon").click();
        }
        addTab(elem);
    });
    // 监听右上角个人导航点击
    element.on('nav(personal)', function (elem) {
        addTab(elem);
    });
    // 监听选项卡切换
    element.on('tab(' + tabLayFilter + ')', function () {
        let layId = $(this).attr("lay-id");
        $(".layui-side ." + classLayuiThis).removeClass(classLayuiThis);
        $(".layui-side a[lay-id=" + layId + "]").parent().addClass(classLayuiThis);
    });
    // 监听选项卡删除
    element.on('tabDelete(' + tabLayFilter + ')', function () {
        let layId = $(this).parent().attr("lay-id");
        $(".layui-side a[lay-id=" + layId + "]").parent().removeClass(classLayuiThis);
    });

    // 默认选中侧边栏第一个菜单
    $(".layui-side a[lay-id]:first").click();

    // 上传头像
    $("#userImg").click(function () {
        top.layer.open({
            type: 2,
            title: '上传头像',
            content: ctx + "/user/uploadUserImgPage",
            area: ['40%', '40%']
        });
    });

    let collapseWidth = 200;
    let $layuiSide = $(".layui-side");
    let $layuiBody = $(".layui-body");
    // 收缩菜单
    $("#collapseIcon").click(function () {
        $layuiSide.width($layuiSide.width() - collapseWidth);
        $layuiSide.find(".layui-nav-item").width($layuiSide.width() - collapseWidth);
        $layuiBody.width($layuiBody.width() + collapseWidth);
        $layuiBody.offset({
            left: $layuiBody.offset().left - collapseWidth
        });
        $layuiSide.find(".layui-nav-child").addClass("hide");

        $layuiSide.find(".layui-nav-tree > li").each(function () {
            LayerUtil.tips($(this).find(".menu-name").html(), $(this));
        });
        $("#collapseIcon,.layui-side .layui-nav-more,.menu-name").hide();
        $("#expandIcon").show();
    });

    // 展开菜单
    $("#expandIcon").click(function () {
        $("#collapseIcon,.layui-side .layui-nav-more,.menu-name").show();
        $layuiSide.find(".layui-nav-item").width($layuiSide.width() + collapseWidth);
        $layuiSide.width($layuiSide.width() + collapseWidth);
        $layuiBody.width($layuiBody.width() - collapseWidth);
        $layuiBody.offset({
            left: $layuiBody.offset().left + collapseWidth
        });
        $layuiSide.find(".layui-nav-child").removeClass("hide");

        $layuiSide.find(".layui-nav-tree > li").each(function () {
            $(this).mouseout().off('mouseover mouseout')
        });

        $("#expandIcon").hide();
    });

    tabRightClickEvent();

    /**
     * 右键菜单事件
     */
    function tabRightClickEvent() {
        let tabElement = document.getElementById("main-tab-title");
        let tabRightClickMenu = document.getElementById("main-tab-menu");
        //去掉默认的contextmenu事件，否则会和右键事件同时出现。
        tabElement.oncontextmenu = function (e) {
            e.preventDefault();
        };
        tabRightClickMenu.oncontextmenu = function (e) {
            e.preventDefault();
        };
        tabElement.onmousedown = function (e) {
            if (e.button === 2) {//右键
                let x = e.clientX;//获取鼠标单击点的X坐标
                let y = e.clientY;//获取鼠标单击点的Y坐标
                //设置菜单的位置
                tabRightClickMenu.style.position = "fixed";
                tabRightClickMenu.style.left = (x - 5) + "px";
                tabRightClickMenu.style.top = (y - 5) + "px";
                tabRightClickMenu.style.display = "block";
                $("#main-tab-menu-header").click();
            } else if (e.button === 0) { //左键
                tabRightClickMenu.style.display = "none";
            } else if (e.button === 1) { //按下滚轮
                tabRightClickMenu.style.display = "none";
            }
        }
        tabRightClickMenu.onmouseleave = function (e) {
            tabRightClickMenu.style.display = "none";
        }

        $("#closeCurrent").click(function () {
            let layId = getCurrentTabId();
            element.tabDelete(tabLayFilter, layId);
            tabRightClickMenu.style.display = "none";
        });

        $("#closeLeft").click(function () {
            let layId = getCurrentTabId();
            $("#main-tab-title li[lay-id='" + layId + "']").prevAll("li").each(function () {
                element.tabDelete(tabLayFilter, $(this).attr("lay-id"));
            });
            tabRightClickMenu.style.display = "none";
        });

        $("#closeRight").click(function () {
            let layId = getCurrentTabId();
            $("#main-tab-title li[lay-id='" + layId + "']").nextAll("li").each(function () {
                element.tabDelete(tabLayFilter, $(this).attr("lay-id"));
            });
            tabRightClickMenu.style.display = "none";
        });

        $("#closeOther").click(function () {
            let layId = getCurrentTabId();
            $("#main-tab-title li[lay-id!='" + layId + "']").each(function () {
                element.tabDelete(tabLayFilter, $(this).attr("lay-id"));
            });
            tabRightClickMenu.style.display = "none";
        });

        $("#closeAll").click(function () {
            $("#main-tab-title li").each(function () {
                element.tabDelete(tabLayFilter, $(this).attr("lay-id"));
            });
            tabRightClickMenu.style.display = "none";
        });
    }

    function getCurrentTabId() {
        return $(".layui-side ." + classLayuiThis + " a").attr("lay-id");
    }

    function addTab(elem) {
        let layId = elem.attr("lay-id");
        if (!layId) {
            return;
        }
        // 增加一个tab
        let layHref = elem.attr("lay-href");
        if ($(".layui-tab li[lay-id=" + layId + "]").length === 0) {
            element.tabAdd(tabLayFilter, {
                title: elem.attr("lay-title") || elem.text(),
                content: '<iframe src="' + layHref + '" frameborder="0" id="iframe-body-' + layId + '" style="width: 100%;height: 100%;"></iframe>',
                id: layId
            });
            // 最多展示多少个tab页
            let $tabLis = $("[lay-filter='main-tab'] ul > li");
            if ($tabLis.length > ${maxTabs!10}) {
                element.tabDelete(tabLayFilter, $tabLis.first().attr("lay-id"));
            }
        } else {
            document.getElementById("iframe-body-" + layId).contentWindow.location.reload();
        }
        element.tabChange(tabLayFilter, layId);
    }

    let socketUrl = CommonUtil.getWsProtocol() + "://" + window.location.host + ctx + "/socket/validateSession";
    let socket = new WebSocket(socketUrl);
    //打开事件
    socket.onopen = function () {

    };
    //获得消息事件
    socket.onmessage = function (msg) {
        let result = JSON.parse(msg.data);
        let code = result.code;
        if (code === 402 || code === 405) {
            top.window.location = ctx + "/loginPage?code=" + code;
        }
    };
    //关闭事件
    socket.onclose = function () {

    };
    //发生了错误事件
    socket.onerror = function () {
        console.log("websocket发生了错误");
    };

    /*
     * 系统消息
     */
    let socketMessageUrl = CommonUtil.getWsProtocol() + "://" + window.location.host + ctx + "/socket/notify";
    let socketMessage = new WebSocket(socketMessageUrl);
    //获得消息事件
    socketMessage.onmessage = function (msg) {
        let result = JSON.parse(msg.data);
        let all = result.all;
        if (all > 0) {
            $("#messageCount").text(all).show();
        } else {
            $("#messageCount").hide();
        }
    };
});
</script>
</body>
</html>
