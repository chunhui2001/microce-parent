1. application 会覆盖 bootstrap 中的相同配置项, 
2. Bootstrap.yml（bootstrap.properties）在 application.yml（application.properties）之前加载
3. 当 .properties 和 .yml 中存在相同的配置项时, 从 .properties 取值
4. 此路径下不要放 log4j2.xml, log4j.xml, bootstrap.yml, bootstrap.properties
5. application.properties 配置所有环境不变的信息, 当 .properties 和 .yml 中存在相同的配置项时, 从 .properties 取值
6. application.yml 线上环境该配置会被覆盖
7. applicationContext-mybatis.xml 需在每个子项目下单独配置