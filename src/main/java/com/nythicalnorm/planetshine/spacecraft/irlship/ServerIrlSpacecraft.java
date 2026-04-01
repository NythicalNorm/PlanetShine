package com.nythicalnorm.planetshine.spacecraft.irlship;

import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;

public class ServerIrlSpacecraft extends AbstractIrlSpacecraft {
    public ServerIrlSpacecraft(IRLSpacecraftBuilder orbitalBuilder) {
        super(orbitalBuilder, false);
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return this.orbitHostSpace.get();
    }
}
