package org.spat.scf.client.loadbalance;

/**
 * ServerState
 *
 * @author Service Platform Architecture Team 
 */
public enum ServerState {

    Dead,
    Normal,
    Busy,
    Disable,
    Reboot,
    Testing,
    Deleted		//标识已从Server列表中删除
}
