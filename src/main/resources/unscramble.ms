
register_channel('bungee:unscramble')

proc(_us_award_item, @sessionId, @playerName, @material, @data, @count,
    assign(@remain, pgive_item(@playerName, concat(data_values(@material),':', @data), @count))
    if(@remain != 0){
        assign(@ba, byte_array())
        ba_put_string(@ba, 'AwardFail')
        ba_put_int(@ba, @sessionId)
        ba_put_byte(@ba, 1)
        ba_put_int(@ba, @remain)
        
        send_plugin_message('bungee:unscramble', @ba)
    }
    else {
        assign(@ba, byte_array())
        ba_put_string(@ba, 'AwardOk')
        ba_put_int(@ba, @sessionId)
        
        send_plugin_message('bungee:unscramble', @ba)
    }
)

proc(_us_award_money, @sessionId, @playerName, @amount,
    acc_add(@playerName, @amount)
    
    assign(@ba, byte_array())
    ba_put_string(@ba, 'AwardOk')
    ba_put_int(@ba, @sessionId)
    
    send_plugin_message('Unscramble', @ba)
)

bind('plugin_message_received', null, array(channel:'bungee:unscramble'), @event,
    assign(@ba, @event['bytes'])
    ba_rewind(@ba)
    
    assign(@subchannel, ba_get_string(@ba))
    assign(@sessionId, ba_get_int(@ba))
    
    if(@subchannel == 'Award$') {
       _us_award_money(@sessionId, ba_get_string(@ba), ba_get_double(@ba))
    }
    else if(@subchannel == 'AwardItem') {
       _us_award_item(@sessionId, ba_get_string(@ba), ba_get_string(@ba), ba_get_byte(@ba), ba_get_byte(@ba))
    }
    else {
        assign(@ba, byte_array())
        ba_put_string(@ba, 'AwardFail')
        ba_put_int(@ba, @sessionId)
        ba_put_byte(@ba, 2)
        
        send_plugin_message('bungee:unscramble', @ba)
    }
)