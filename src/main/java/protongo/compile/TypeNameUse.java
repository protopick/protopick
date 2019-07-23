package protongo.compile;


public enum TypeNameUse {
    TYPE_TOP_LEVEL {
        public boolean definesNewType() {return true;}
    },
    TYPE_LOWER_LEVEL {
        public boolean definesNewType() {return true;}
    },
    FIELD {
        public boolean mayBeRelative() {return true;}
    },
    FIELD_SIMPLE {
    };

    public boolean definesNewType() {return false;}
    public boolean mayBeRelative() {return false;}
}
