# IpBlockFilter
Servlet Ip Block Filter 

# 使い方

`IpBlockerFilter`を`ServletFilter`に追加  
パラメータを設定  


 パラメータ     | 内容                        | デフォルト値  
--------------+-----------------------------+------------  
 timeWindowMs | リクエストを受け付ける時間(ms)  | 60,000 (10分)  
 count        | リクエストを受け付ける数        |     10  
 blockingTimeMs | ブロック後に止める時間       | 360,000 (1時間)  
 maxIpSize    | 管理するIPサイズ              |  10,000  
 blockingStatus | ブロックしたときのステータスコード |  403

# お勧めの使い方

## 独自Filterの作成

`IpBlockerFilter`を継承した独自のFilterを作成  
`blockResponse`と`useCalcBlock`を独自に実装する  

## Filterのネスト

多段でFilterを設定することも出来る

