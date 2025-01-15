package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {
    private final NacosConfigManager nacosConfigManager;
    private final String dataId = "gateway-router.json";
    private final String group = "DEFAULT_GROUP";

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct //bean初始化完成之后执行
    public void initRouterConfigListener() throws NacosException {
        //1.项目启动，先拉取配置，并且添加配置监听器
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(dataId, group, 3000, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                //2.监听到配置变化，重新加载路由
                updateConfigInfo(configInfo);
            }
        });
        //3.第一次读取到配置，也要更新到路由表
        updateConfigInfo(configInfo);
    }
    private void updateConfigInfo(String configInfo) {
        log.debug("路由配置信息：{}",configInfo);
        //1.解析json,转化为RouterDefinition对象
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //2.根据Id删除路由表
        routeIds.forEach(id -> {
            routeDefinitionWriter.delete(Mono.just(id)).subscribe();
                });
        routeIds.clear();
        //3.更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            //3.1更新路由（mono响应式容器，subscribe()为订阅）
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            //3.2保存路由id,方便删除
            routeIds.add(routeDefinition.getId());
        }

    }

}
