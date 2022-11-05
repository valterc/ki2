// IMessageCallback.aidl
package com.valterc.ki2.services.callbacks;

import com.valterc.ki2.data.message.Message;

interface IMessageCallback {

    void onMessage(in Message message);

}