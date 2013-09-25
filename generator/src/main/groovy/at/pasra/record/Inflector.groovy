package at.pasra.record

import java.util.regex.Pattern

/**
 * Created by rich on 9/8/13.
 */
class Inflector {

    static Inflector mInstance;
    static {
        mInstance = new Inflector()
    }

    static String singularize(String name) {
        if (name.startsWith('#')) {
            return name.substring(1)
        }

        def singular = mInstance.irregularPlural[name.toLowerCase()]
        if (singular) {
            return singular;
        }

        for (e in mInstance.regularSingular) {
            def regex = e[0]
            def replace = e[1]

            def matcher = name =~ regex
            if (matcher.find()) {
                def builder = new StringBuffer();
                matcher.appendReplacement(builder, replace)
                matcher.appendTail(builder)
                return builder.toString()
            }
        }

        return name;
    }

    static String pluralize(String name) {
        def plural = mInstance.irregularSingular[name.toLowerCase()]
        if (plural) {
            return plural;
        }

        for (e in mInstance.regularPlural) {
            def regex = e[0]
            def replace = e[1]

            def p = Pattern.compile(regex)
            def m = p.matcher(name.toLowerCase())
            if (m.find()) {
                def b = new StringBuffer();
                m.appendReplacement(b, replace)
                m.appendTail(b)
                return b.toString()
            }
        }

        return name;
    }


    static String tabelize(String name) {
        StringBuilder builder = new StringBuilder();

        boolean nextScore = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (Character.isUpperCase(c) && i != 0) {
                builder.append('_')
            }
            builder.append(Character.toLowerCase(c));
        }

        return builder.toString();
    }

    static String camelize(String name) {
        StringBuilder builder = new StringBuilder();

        boolean nextUp = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                nextUp = true;
                continue;
            }
            if (nextUp) {
                builder.append(Character.toUpperCase(c));
                nextUp = false;
            } else {
                builder.append(c);
            }

        }

        return builder.toString()
    }

    def irregularSingular = [:]
    def irregularPlural = [:]
    def regularSingular = []
    def regularPlural = []

    public Inflector() {

        // FROM https://github.com/rails/rails/blob/feaa6e2048fe86bcf07e967d6e47b865e42e055b/activesupport/lib/active_support/inflections.rb
        this.plural(/$/, 's')
        this.plural(/s$/, 's')
        this.plural(/^(ax|test)is$/, '\$1es')
        this.plural(/(octop|vir)us$/, '\$1i')
        this.plural(/(octop|vir)i$/, '\$1i')
        this.plural(/(alias|status)$/, '\$1es')
        this.plural(/(bu)s$/, '\$1ses')
        this.plural(/(buffal|tomat)o$/, '\$1oes')
        this.plural(/([ti])um$/, '\$1a')
        this.plural(/([ti])a$/, '\$1a')
        this.plural(/sis$/, 'ses')
        this.plural(/(?:([^f])fe|([lr])f)$/, '\$1\$2ves')
        this.plural(/(hive)$/, '\$1s')
        this.plural(/([^aeiouy]|qu)y$/, '\$1ies')
        this.plural(/(x|ch|ss|sh)$/, '\$1es')
        this.plural(/(matr|vert|ind)(?:ix|ex)$/, '\$1ices')
        this.plural(/^(m|l)ouse$/, '\$1ice')
        this.plural(/^(m|l)ice$/, '\$1ice')
        this.plural(/^(ox)$/, '\$1en')
        this.plural(/^(oxen)$/, '\$1')
        this.plural(/(quiz)$/, '\$1zes')

        this.singular(/s$/, '')
        this.singular(/(ss)$/, '\$1')
        this.singular(/(n)ews$/, '\$1ews')
        this.singular(/([ti])a$/, '\$1um')
        this.singular(/((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)(sis|ses)$/, '\$1sis')
        this.singular(/(^analy)(sis|ses)$/, '\$1sis')
        this.singular(/([^f])ves$/, '\$1fe')
        this.singular(/(hive)s$/, '\$1')
        this.singular(/(tive)s$/, '\$1')
        this.singular(/([lr])ves$/, '\$1f')
        this.singular(/([^aeiouy]|qu)ies$/, '\$1y')
        this.singular(/(s)eries$/, '\$1eries')
        this.singular(/(m)ovies$/, '\$1ovie')
        this.singular(/(x|ch|ss|sh)es$/, '\$1')
        this.singular(/^(m|l)ice$/, '\$1ouse')
        this.singular(/(bus)(es)?$/, '\$1')
        this.singular(/(o)es$/, '\$1')
        this.singular(/(shoe)s$/, '\$1')
        this.singular(/(cris|test)(is|es)$/, '\$1is')
        this.singular(/^(a)x[ie]s$/, '\$1xis')
        this.singular(/(octop|vir)(us|i)$/, '\$1us')
        this.singular(/(alias|status)(es)?$/, '\$1')
        this.singular(/^(ox)en/, '\$1')
        this.singular(/(vert|ind)ices$/, '\$1ex')
        this.singular(/(matr)ices$/, '\$1ix')
        this.singular(/(quiz)zes$/, '\$1')
        this.singular(/(database)s$/, '\$1')

        this.irregular('person', 'people')
        this.irregular('man', 'men')
        this.irregular('child', 'children')
        this.irregular('sex', 'sexes')
        this.irregular('move', 'moves')
        this.irregular('cow', 'kine')
        this.irregular('zombie', 'zombies')

        ["equipment", "information", "rice", "money", "species", "series", "fish", "sheep", "jeans", "police"].each { u ->
            this.uncountable(u)
        }

        regularPlural = regularPlural.reverse()
        regularSingular = regularSingular.reverse()
    }

    public void plural(String regex, String replace) {
        regularPlural << [regex, replace];
    }

    public void singular(String regex, String replace) {
        regularSingular << [regex, replace];
    }

    public void irregular(String singular, String plural) {
        irregularSingular[singular] = plural;
        irregularPlural[plural] = singular;
    }

    public void uncountable(String name) {
        irregularSingular[name] = name;
        irregularPlural[name] = name;
    }
}
