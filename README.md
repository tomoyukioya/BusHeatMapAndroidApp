# BusHeatMapAndroidApp

全国のバス停をヒートマップ形式でGoogle Map上に表示するAdroidアプリです。
<https://github.com/tomoyukioya/BusHeatMapServer.git>
と対になって動作します。

## 使っているもの

国土交通省の国土数値情報のうち、バス停留所データ（<http://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-P11.html>）。
約25.5万件のバス停情報があります。

## 何が表示されるのか？

各バス停情報に、固定の強度値をランダムに割り当てています。アプリには、以下の４つのヒートマップ表示モードがあります。

* _HeatMap(Weighted)_：
Google Map Android APIの HeatmapTileProviderを使用して、強度値に応じたWeightedDataを表示します。Mapの表示を変更するたびに、
都度、サーバから、表示範囲内のバス停情報をダウンロードします。地図を縮小すると、ダウンロードすべきデータ量が膨大になるので、
強度値が高い方から一定数（デフォルト設定で1,000個）のバス停情報のみ抽出してダウンロードします。

比較的滑らかに動作しますが、地図を拡大した際のヒートマップ表示がイメージと合わないかもしれません。

* _Mesh(1k)_：
1kmメッシュ内の平均強度値に応じて、濃さを変化させた着色表示を行います。地図を縮小すると、
Android側のメッシュレンダリングが重くなって使い物にならないので、ある程度以上の地図縮小ができないよう制限しています。

* _Mesh(500m)_：
500mメッシュです。1kmメッシュの場合より、地図縮小を制限しています。

* _Mesh(可変)_
地図を拡大していくと、最少500mメッシュの表示を行います。地図を縮小していくと、ズームレベルに応じたメッシュサイズになります。
地図を縮小すると、ダウンロードすべきデータ量が膨大になるので、強度値が高い方から一定数（デフォルト設定で1,000個）
のバス停情報のみ抽出してダウンロードします。

## 使い方(apkダウンロード)

<https://trafficmap.blob.core.windows.net/apk/32826cbb7bc16745df06adae7e1f66c0.gif>
で表示されるQRコードからアプリをインストールしてください。

## 使い方（ソースビルド）

clone したままでは動きません。    
Google Map API Keyを記述した `/app/src/debug/res/values/google_maps_api.xml` と、
`/app/src/release/res/values/google_maps_api.xml` ファイルを gitignore しているので、     
clone したら そのファイルを作ってGoogle Map API Keyを記述してください。

````xml
<resources>
  <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
      YOUR GOOGLE MAP API KEY HERE
  </string>
</resources>
````

# TODO:

* メッシュを表示するとき、きっちり表示範囲分のバス停データしか要求していないので、表示境界のメッシュの平均強度が正確ではない。
* 縦画面と横画面を切り替えると、ヒートマップ表示モードがリセットされる。
* onCellInfoChanged()が時々しか呼ばれない。情報更新頻度の設定があるのか？？googleってもよくわからん。
