<template>


  <div class="common-layout">
    <el-container>
      <el-header class="header">netty-ui</el-header>
      <el-container>

        <el-container>
          <el-main>
            <div class="leftContainer">
              <el-text>服务器配置 状态: {{ connectState ? '连接成功' : '尚未连接' }}</el-text>

              <el-form :model="form" label-width="auto" style="max-width: 600px">

                <el-form-item label="服务配置">
                  <div style="max-width: 500px">
                    <el-input
                        v-model="serveUrl"
                        style="max-width: 250px"
                        placeholder="Please input"
                    >
                      <template #prepend>服务地址</template>
                    </el-input>
                    <el-button type="primary" @click="connect">{{ connectState ? '关闭连接' : '开启连接' }}</el-button>
                  </div>
                </el-form-item>

                <el-form-item label="发包设置">
                  <div class="packageSettings">
                    <el-input
                        v-model="form.interval"
                        style="max-width: 150px"
                        placeholder="Please input"
                    >
                      <template #prepend>每隔</template>
                    </el-input>

                    <el-input
                        v-model="form.intervalSendContext"
                        style="max-width: 600px"
                        placeholder="Please input"
                    >
                      <template #prepend>秒发送内容</template>
                    </el-input>
                    <el-switch
                        style="display: block"
                        v-model="form.intervalSwitch"
                        active-color="#13ce66"
                        inactive-color="#ff4949"
                        @change="changeIntervalSwitch">
                    </el-switch>
                  </div>

                </el-form-item>


              </el-form>
            </div>

            <div class="chatDetail">

              <el-scrollbar height="300px">
                <div v-for="(item, index) in dataList" :key="item.uuid" class="scrollbar-demo-item">
                  <div :class="item.type">
                    <span v-if="item.type === 'send'">
                      <span v-if="item.sendResult"> ✔ </span>
                      <span v-else> × </span>
                    </span>

                    {{ item.time }} <br/>
                    {{ item.context }}
                  </div>
                </div>
              </el-scrollbar>
            </div>

          </el-main>
          <el-footer style="width: 80%">
            <el-input
                v-model="context"
                style="width: 80%"
                :rows="2"
                type="textarea"
                placeholder="Please input"
                clearable
            />
            <el-button type="success" @click="sendMsg">发送</el-button>
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>

</template>

<script>

import {initMyWebSocket, WebSocketOptType, MessageType} from '@/components/webSocket'
import {getUuid, formatDate, isEmpty, isNotEmpty} from '@/components/utils'

import {ElMessage} from 'element-plus'


export default {
  name: 'HelloWorld',

  data() {
    return {
      currentWebSocket: null,
      connectState: false,
      serveUrl: 'ws://127.0.0.1:8351?Authorization=eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2lkIjoiMTExIn0.jKD77D3dCFoRRvPWCclBVU754_mfhrlnALMvd5NuHkepYk5g8w1DQYFBiA54bKl4zJCQON13Ptacw1tov-bKRQ',
      form: {
        interval: 5,
        intervalSendContext: 'Ping',
        intervalSwitch: false,
      },
      dataList: [
        {
          uuid: '1',
          type: 'send',
          sendResult: false, // 发送结果
          time: '11:11:11',
          context: '消息'
        }, {
          uuid: '2',
          type: 'receive',
          sendResult: false, // 发送结果
          time: '11:22:11',
          context: '消息2'
        }, {
          uuid: '3',
          type: 'send',
          sendResult: false, // 发送结果
          time: '11:44:11',
          context: '消3息'
        }
      ],
      context: ''
    }
  },
  methods: {
    /**
     * 连接websocket
     */
    connect() {
      if (!this.connectState) {
        // 开启连接
        // 把this.receiveMsg传入websocket里面当右消息来的时候会回调这个函数
        const myWebSocket = initMyWebSocket(this.serveUrl, this.receiveMsg)
        if (isNotEmpty(myWebSocket)) {
          this.currentWebSocket = myWebSocket
        } else {
          console.log('连接失败')
        }
      } else {
        // 关闭连接
        this.currentWebSocket.close()
        this.currentWebSocket = null;
      }
    },

    /**
     * 心跳开关
     */
    changeIntervalSwitch(value) {
      this.form.intervalSwitch = value
      if (value) {
        this.currentWebSocket.startHeartbeat(this.form.interval, this.form.intervalSendContext)
      } else {
        this.currentWebSocket.stopHeartbeat()
      }
    },

    /**
     * 发送消息
     */
    sendMsg() {
      if (isEmpty(this.currentWebSocket)) {
        ElMessage.error('websocket未连接')
      } else {
        console.log(this.context)
        const msg = {
          uuid: getUuid(),
          time: formatDate(),
          messageType: MessageType.CHAT,
          userId: 123,
          destUserId: 666,
          context: this.context
        }
        this.currentWebSocket.send(msg)

        msg.type = 'send'
        msg.sendResult = false // 发送结果

        // 往聊天记录窗口追加数据
        this.dataList.push(msg)

      }
    },

    /**
     * 接收到消息
     */
    receiveMsg(type, data = null) {
      if (WebSocketOptType.Open === type) {
        this.connectState = true
      } else if (WebSocketOptType.Close === type) {
        this.connectState = false
      } else if (WebSocketOptType.Message === type) {
        var res = JSON.parse(data);
        switch (res.messageType) {
          case MessageType.SEND_RESULT:
            // 弹窗显示消息发送结果
            if (res.context) {
              // 使用map()方法创建新数组
              this.dataList = this.dataList.map(function (item) {
                // 如果uuid为"2"，则修改sendResult属性为true，否则保持不变
                return item.uuid === res.uuid ? {...item, sendResult: true} : item;
              });
              ElMessage({
                message: '消息发送成功',
                type: 'success',
              })
            } else {
              ElMessage.error('消息发送失败')
            }
            break;
          case MessageType.ERROR:
            ElMessage.error(res.context)
            break;
          case MessageType.CHAT:
            console.log('MessageType.CHAT', res)
            res.type = 'receive'
            this.dataList.push(res)
            break;
          case MessageType.MULTI_CHAT:
            break;
          default:
            console.log('receiveMsg', res)
        }

      }
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  display: inline-block;
  margin: 0 10px;
}

a {
  color: #42b983;
}

.header {
  background-color: #42b983;
}

.packageSettings {
  display: flex;
  justify-content: center;
}

div {
  /*display: flex;*/
  justify-content: space-between; /* 内容向两端对齐 */
}

.send {
  text-align: right;
  background-color: #c7d9d3;
}

.receive {
  text-align: left;
  background-color: #e5d8e5;
}

.scrollbar-demo-item {
  /*display: flex;*/
  /*align-items: center;*/
  /*justify-content: center;*/
  height: 50px;
  margin: 10px;
  text-align: center;
  border-radius: 4px;
  /*background: var(--el-color-primary-light-9);*/
  /*color: var(--el-color-primary);*/
}
</style>
