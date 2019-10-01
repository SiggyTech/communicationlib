package com.siggytech.utils.notificatorlib.greendao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by fsoto on 9/29/19.
 */
@Entity(nameInDb = "destination")
public class Destination {
    @Id
    private Long id;

    @Property(nameInDb = "name")
    private String name;

    @Property(nameInDb = "ip")
    private String ip;

    @Property(nameInDb = "port")
    private int port;

    @Property(nameInDb = "idgroup")
    private int idgroup;

    @Generated(hash = 345438490)
    public Destination(Long id, String name, String ip, int port, int idgroup) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.idgroup = idgroup;
    }

    @Generated(hash = 703305004)
    public Destination() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getIdgroup() {
        return this.idgroup;
    }

    public void setIdgroup(int idgroup) {
        this.idgroup = idgroup;
    }
}
