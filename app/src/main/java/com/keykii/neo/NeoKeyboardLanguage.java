package com.keykii.neo;

/** The existing language row definitions, shared without changing their key order. */
final class NeoKeyboardLanguage {
    static String label(String activeKeyboardLanguage) {
        switch(activeKeyboardLanguage) {
            case "en-GB":
                return "English (UK)";
            case "fil":
                return "Filipino";
            case "ceb":
                return "Cebuano";
            case "es":
                return "Español";
            case "fr":
                return "Français";
            case "de":
                return "Deutsch";
            case "tr":
                return "Türkçe";
            case "pt":
                return "Português";
            case "it":
                return "Italiano";
            case "en-US":
            default:
                return "English";
        }
    }


    static String[] top(String activeKeyboardLanguage) {
        switch(activeKeyboardLanguage) {
            case "fr":
                return new String[]{
                    "a","z","e","r","t",
                    "y","u","i","o","p"
                };

            case "de":
                return new String[]{
                    "q","w","e","r","t",
                    "z","u","i","o","p"
                };

            case "tr":
                return new String[]{
                    "q","w","e","r","t",
                    "y","u","ı","o","p","ğ","ü"
                };

            default:
                return new String[]{
                    "q","w","e","r","t",
                    "y","u","i","o","p"
                };
        }
    }


    static String[] middle(String activeKeyboardLanguage) {
        switch(activeKeyboardLanguage) {
            case "fr":
                return new String[]{
                    "q","s","d","f","g",
                    "h","j","k","l","m"
                };

            case "es":
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l","ñ"
                };

            case "pt":
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l","ç"
                };

            case "tr":
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l","ş","i"
                };

            default:
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l"
                };
        }
    }


    static String[] bottom(String activeKeyboardLanguage) {
        switch(activeKeyboardLanguage) {
            case "fr":
                return new String[]{
                    "w","x","c","v","b","n"
                };

            case "de":
                return new String[]{
                    "y","x","c","v","b","n","m"
                };

            case "tr":
                return new String[]{
                    "z","x","c","v",
                    "b","n","m","ö","ç"
                };

            default:
                return new String[]{
                    "z","x","c","v",
                    "b","n","m"
                };
        }
    }


    static String hint(String s){

        switch(s){

            case "q": return "1";
            case "w": return "2";
            case "e": return "3";
            case "r": return "4";
            case "t": return "5";
            case "y": return "6";
            case "u": return "7";
            case "i": return "8";
            case "o": return "9";
            case "p": return "0";

            case "a": return "@";
            case "s": return "#";
            case "d": return "$";
            case "f": return "%";
            case "g": return "&";
            case "h": return "-";
            case "j": return "+";
            case "k": return "(";
            case "l": return ")";

            case "z": return "*";
            case "x": return "\"";
            case "c": return "'";
            case "v": return ":";
            case "b": return ";";
            case "n": return "!";
            case "m": return "?";
            case ",": return "☺";

            default: return "";
        }
    }

}
