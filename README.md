# crawler-denfender

java web系统的反网页爬虫程序

简介：
一些智能的搜索引擎爬虫的爬取频率比较合理，对网站资源消耗比较少，但是很多糟糕的网络爬虫，对网页爬取能力很差，经常并发几十上百个请求循环重复抓取，这种爬虫对中小型网站往往是毁灭性打击，特别是一些缺乏爬虫编写经验的程序员写出来的爬虫破坏力极强，造成的网站访问压力会非常大，会导致网站访问速度缓慢，甚至无法访问。本程序智能识别爬虫，防止爬虫对系统造成大的负载，也可用于访问请求的限流。

爬虫识别策略：
1.实时策略：访问者ip单位时间内访问次数，超过设定阀值的ip列入观察名单；观察名单中的访问着在下一单位时间内继续访问则要求其   填写验证码，若没有填写验证码而持续发起大量请求，则判定为爬虫，加入黑名单。
2.离线策略：引入访问统计系统，对访问记录进行持久化，按分、小时、天等维度进行分析，超过阀值的ip列入黑名单；
3.爬虫陷阱：设置爬虫陷阱，爬进陷阱的ip列入黑名单

总体设计分三部分：
1.counter：计数器，用于实时记录单位时间数内ip的访问次数，可以使用hashmap、memcache等实现

2.persistence：日志持久化，可以是db、filesystem等，负责日志的持久化以及小时、天级别的访问计数

3.blocker：拦截器，
 (1)实时拦截，根据计数器获取访问次数，如果单个ip在单位时间内超过设定阀值，则拦截并返回验证页面，若超过10次未填写      验证页面则将此ip放入黑名单
 (2)：拦截黑名单中的ip
 (3)：维护黑、白名单：分析访问日志，生成黑名单

使用：
1.配置web.xml

<filter>
    <filter-name>crawler_defender_filter</filter-name>
    <filter-class>com.xycode.crawlerdefender.filter.GlobalFilter</filter-class>
    <init-param>
    	<param-name>configFile</param-name>
    	<param-value>classpath:crawler-defender.xml</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>crawler_defender_filter</filter-name>
    <url-pattern>*.jsp</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>crawler_defender_filter</filter-name>
    <url-pattern>*.html</url-pattern>
  </filter-mapping>
  
  <servlet>
    <servlet-name>crawler_defender_logger_servlet</servlet-name>
    <servlet-class>com.xycode.crawlerdefender.servlet.LoggerServlet</servlet-class>
    <load-on-startup>2</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>crawler_defender_logger_servlet</servlet-name>
    <url-pattern>/crawler-defender/logger</url-pattern>
  </servlet-mapping>
  
  <servlet>
    <servlet-name>crawler_defender_validator_servlet</servlet-name>
    <servlet-class>com.xycode.crawlerdefender.servlet.ValidatorServlet</servlet-class>
    <load-on-startup>2</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>crawler_defender_validator_servlet</servlet-name>
    <url-pattern>/crawler-defender/validator</url-pattern>
  </servlet-mapping>
  
  2.配置crawler-defender.xml
  
<?xml version="1.0" encoding="UTF-8"?>
<config>
	<!-- 拦截校验配置，超过阀值拦截并返回校验页面  -->
	<block-validate>
		<!-- 访问阀值设置 -->
		<thresholds>
			<threshold level="seconds" duration="1" value="3" /><!-- 3秒钟内访问阀值为10次 -->
			<threshold level="minutes" duration="1" value="30" /><!-- 1分钟内访问阀值为30次 -->
			<threshold level="hours" duration="1" value="200" /><!-- 1小时内访问阀值为200次 -->
			<threshold level="days" duration="1" value="500" /><!-- 1天内访问阀值为500次 -->
		</thresholds>
		<!-- 拦截后校验设置，可以设置为自定义页面 -->
		<validatorPath>/crawler-defender/validator-page?action=page</validatorPath>
	</block-validate>
	
	<!-- ip白名单,不拦截。ip黑白名单的优先级高于user-agent -->
	<ipWhiteList>
		<item>127.0.0.1</item>
		<item>61.135.168.*</item>
	</ipWhiteList>
	
	<!-- ip黑名单，直接返回401 -->
	<ipBlackList>
		<item>192.168.1.102</item>
	</ipBlackList>
	
	<!-- userAgent白名单, 对于此白名单的爬虫，延迟10秒返回页面，以减轻对网站的负载 -->
	<userAgentWhiteList>
		<item>Baiduspider+</item>
		<item>qihoobot</item>
		<item>Googlebot</item>
		<item>YodaoBot</item>
		<item>msnbot</item>
		<item>Yahoo</item>
	</userAgentWhiteList>
	
	<!-- userAgent黑名单，直接返回401 -->
	<userAgentBlackList>
		<item>Commons-HttpClient</item>
		<item>python</item>
	</userAgentBlackList>
	
	<!-- 日志持久化配置 -->
	<persistence>
		<mongo>
			<serverAddress>localhost:27017</serverAddress>
			<dbname>test</dbname>
			<username>root</username>
			<password>xy123456</password>
		</mongo>
	</persistence>
	
	<!-- 是否开启爬虫陷阱 -->
	<bot-trap>true</bot-trap>
	
</config>



