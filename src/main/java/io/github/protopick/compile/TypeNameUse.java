package io.github.protopick.compile;


public enum TypeNameUse {
    PACKAGE {
        public boolean registersWithToken() {return true;}
    },
    TYPE_TOP_LEVEL {
        public boolean registersWithToken() {return true;}
        public boolean definesNewType() {return true;}
    },
    TYPE_LOWER_LEVEL {
        public boolean registersWithToken() {return true;}
        public boolean definesNewType() {return true;}
    },
    FIELD {
        public boolean mayBeRelative() {return true;}
    },
    FIELD_PRIMITIVE {
        public boolean isPrimitive() {return true;}
    };

    public boolean registersWithToken() {return false;}
    public boolean definesNewType() {return false;}
    public boolean mayBeRelative() {return false;}
    public boolean isPrimitive() {return false;}
}
