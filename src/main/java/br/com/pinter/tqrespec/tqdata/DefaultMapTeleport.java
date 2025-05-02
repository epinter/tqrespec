/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Respec.

    TQ Respec is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Respec is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Respec.  If not, see <http://www.gnu.org/licenses/>.
*/

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqrespec.save.UID;

import java.util.ArrayList;
import java.util.List;

public class DefaultMapTeleport extends MapTeleport {
    private static List<MapTeleport> tp = new ArrayList<>();

    static {
        /* Greece */
        tp.add(new DefaultMapTeleport(0, new UID("4136144580-999965812-3093316465-1160239764"),
                DefaultAct.GREECE, "RECORDS\\ITEM\\SHRINES\\TELEPORTGREECE\\TELEPORTSHRINEHELIOS01.DBR"));
        tp.add(new DefaultMapTeleport(1, new UID("2138270748-2277723379-2649935820-2482541159"),
                DefaultAct.GREECE, "RECORDS\\ITEM\\SHRINES\\TELEPORTGREECE\\TELEPORTSHRINESPARTA01.DBR"));
        tp.add(new DefaultMapTeleport(2, new UID("1033186033-636833241-2587155753-10827625"),
                DefaultAct.GREECE, "RECORDS\\ITEM\\SHRINES\\TELEPORTGREECE\\TELEPORTSHRINEMEGARA01.DBR"));
        tp.add(new DefaultMapTeleport(3, new UID("3884486806-1285375082-2501517569-3470666036"),
                DefaultAct.GREECE, "RECORDS\\ITEM\\SHRINES\\TELEPORTGREECE\\TELEPORTSHRINEDELPHI01.DBR"));
        tp.add(new DefaultMapTeleport(4, new UID("3414707915-1609451307-2421845018-733957875"),
                DefaultAct.GREECE, "RECORDS\\ITEM\\SHRINES\\TELEPORTGREECE\\TELEPORTSHRINEMONSTER01.DBR"));
        tp.add(new DefaultMapTeleport(5, new UID("732368439-2800828508-3028823837-3149072738"),
                DefaultAct.GREECE, "RECORDS\\ITEM\\SHRINES\\TELEPORTGREECE\\TELEPORTSHRINEATHENS01.DBR"));
        tp.add(new DefaultMapTeleport(6, new UID("1778331408-1269254134-2643470825-1377610659"),
                DefaultAct.GREECE, "RECORDS\\ITEM\\SHRINES\\TELEPORTGREECE\\TELEPORTSHRINEKNOSSOS01.DBR"));

        /* Egypt */
        tp.add(new DefaultMapTeleport(7, new UID("1581749252-1403077375-2713893004-1303306079"),
                DefaultAct.EGYPT, "RECORDS\\ITEM\\SHRINES\\TELEPORTEGYPT\\TELEPORTSHRINERHAKOTIS01.DBR"));
        tp.add(new DefaultMapTeleport(8, new UID("3787412225-2633911644-2199165519-3399940741"),
                DefaultAct.EGYPT, "RECORDS\\ITEM\\SHRINES\\TELEPORTEGYPT\\TELEPORTSHRINEABEDJU01.DBR"));
        tp.add(new DefaultMapTeleport(9, new UID("176725373-2477277571-2758197389-1463818488"),
                DefaultAct.EGYPT, "RECORDS\\ITEM\\SHRINES\\TELEPORTEGYPT\\TELEPORTSHRINEMEMPHIS01.DBR"));
        tp.add(new DefaultMapTeleport(10, new UID("724576440-2493989337-3186136208-2186069892"),
                DefaultAct.EGYPT, "RECORDS\\ITEM\\SHRINES\\TELEPORTEGYPT\\TELEPORTSHRINEGIZA01.DBR"));
        tp.add(new DefaultMapTeleport(11, new UID("1605684286-1305431995-2294670250-2538785582"),
                DefaultAct.EGYPT, "RECORDS\\ITEM\\SHRINES\\TELEPORTEGYPT\\TELEPORTSHRINEFAYUM01.DBR"));
        tp.add(new DefaultMapTeleport(12, new UID("4011380894-1052262932-2493615659-2270379052"),
                DefaultAct.EGYPT, "RECORDS\\ITEM\\SHRINES\\TELEPORTEGYPT\\TELEPORTSHRINETHEBES01.DBR"));
        tp.add(new DefaultMapTeleport(13, new UID("2959697151-3309194469-3191194341-3936110244"),
                DefaultAct.EGYPT, "RECORDS\\ITEM\\SHRINES\\TELEPORTEGYPT\\TELEPORTSHRINEVOK01.DBR"));

        /* Orient */
        tp.add(new DefaultMapTeleport(14, new UID("2774936561-3036300164-2644596287-1444335191"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINEHANGINGGARDENS01.DBR"));
        tp.add(new DefaultMapTeleport(15, new UID("4285000137-3945286832-2673045522-1268001340"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINEBABYLON01.DBR"));
        tp.add(new DefaultMapTeleport(16, new UID("51807252-3179169983-2749334980-3499461487"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINESILKROADWEST.DBR"));
        tp.add(new DefaultMapTeleport(17, new UID("750018137-214191261-2955155584-1276288377"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINESILKROADEAST.DBR"));
        tp.add(new DefaultMapTeleport(18, new UID("3145054152-3058452431-2191850586-3096229715"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINEGREATWALL01.DBR"));
        tp.add(new DefaultMapTeleport(19, new UID("1589271040-944325970-2811469338-1076726784"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINECHANGAN01.DBR"));
        tp.add(new DefaultMapTeleport(20, new UID("3290304783-301879256-3001969010-1739418192"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINEJADEPALACE01.DBR"));
        tp.add(new DefaultMapTeleport(21, new UID("592466731-3919137702-2639840988-3733631632"),
                DefaultAct.ORIENT, "RECORDS\\ITEM\\SHRINES\\TELEPORTORIENT\\TELEPORTSHRINETOMB01.DBR"));
        tp.add(new DefaultMapTeleport(22, new UID("1216151612-4061611769-2225668276-824684926"),
                DefaultAct.ORIENT, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINEOLYMPUS01.DBR"));

        /* Hades */
        tp.add(new DefaultMapTeleport(23, new UID("670636066-423839979-2202083455-2021834502"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINERHODES01.DBR"));
        tp.add(new DefaultMapTeleport(24, new UID("2171916760-4113581492-2279486569-1416077967"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINEMEDEA01.DBR"));
        tp.add(new DefaultMapTeleport(25, new UID("2509722166-2699906297-2378870338-2669149733"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINEEPIRUS01.DBR"));
        tp.add(new DefaultMapTeleport(26, new UID("1832741423-1705135030-2348798894-3732044820"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINESTYX01.DBR"));
        tp.add(new DefaultMapTeleport(27, new UID("1093279110-1665418532-2826916318-2576678701"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINESTONECITY01.DBR"));
        tp.add(new DefaultMapTeleport(28, new UID("2628915866-1514161539-2622036344-1541385507"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINEJUDGMENT01.DBR"));
        tp.add(new DefaultMapTeleport(29, new UID("1881588706-3792126589-2636453803-1778603759"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINEELYISUM01.DBR"));
        tp.add(new DefaultMapTeleport(30, new UID("137995607-2789228967-2353681595-2123008457"),
                DefaultAct.HADES, "RECORDS\\XPACK\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINEHADESPALACE01.DBR"));

        /* North (DLC) */
        tp.add(new DefaultMapTeleport(31, new UID("2877415073-2592098268-3185088016-2311094323"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_01_CORINTH.DBR"));
        tp.add(new DefaultMapTeleport(32, new UID("3563621538-3696312644-2753290301-1352021627"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_02_HEUNEBURG.DBR"));
        tp.add(new DefaultMapTeleport(33, new UID("3113395849-3999092708-2364019788-893721110"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_03_GLAUBERG.DBR"));
        tp.add(new DefaultMapTeleport(34, new UID("710463028-1205421814-3009736518-4273624374"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_04_GYLFI.DBR"));
        tp.add(new DefaultMapTeleport(35, new UID("600624512-2711701924-2197244444-325181789"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_05_DVERGR.DBR"));
        tp.add(new DefaultMapTeleport(36, new UID("552110786-1716997817-2703936735-413875821"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_06_ASGARD.DBR"));
        tp.add(new DefaultMapTeleport(37, new UID("396366379-1550467908-2211573290-3402340653"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_07_JOTUNHEIM.DBR"));
        tp.add(new DefaultMapTeleport(38, new UID("546149880-957302632-2542004200-2749713300"),
                DefaultAct.NORTH, "RECORDS\\XPACK2\\ITEM\\SHRINES\\TELEPORT\\TELEPORTSHRINE_08_MUSPELHEIM.DBR"));

        /* Atlantis (DLC) */
        tp.add(new DefaultMapTeleport(39, new UID("1966611454-3304147618-3150562645-2114038776"),
                DefaultAct.ATLANTIS, "RECORDS\\XPACK3\\ITEMS\\SHRINES\\TELEPORT\\TELEPORTSHRINE_01_GADIR.DBR"));
        tp.add(new DefaultMapTeleport(40, new UID("1448735239-3194112442-2512013669-2010521008"),
                DefaultAct.ATLANTIS, "RECORDS\\XPACK3\\ITEMS\\SHRINES\\TELEPORT\\TELEPORTSHRINE_02_MALTA.DBR"));
        tp.add(new DefaultMapTeleport(41, new UID("1542479977-1540704435-3135668517-2560330465"),
                DefaultAct.ATLANTIS, "RECORDS\\XPACK3\\ITEMS\\SHRINES\\TELEPORT\\TELEPORTSHRINE_03_ATLAS.DBR"));
        tp.add(new DefaultMapTeleport(42, new UID("1611104465-1069368133-2373739416-1068138977"),
                DefaultAct.ATLANTIS, "RECORDS\\XPACK3\\ITEMS\\SHRINES\\TELEPORT\\TELEPORTSHRINE_03B_MUDSHOALS.DBR"));
        tp.add(new DefaultMapTeleport(43, new UID("2283171435-1568491469-3213599093-3000569402"),
                DefaultAct.ATLANTIS, "RECORDS\\XPACK3\\ITEMS\\SHRINES\\TELEPORT\\TELEPORTSHRINE_04_OUTERATLANTIS.DBR"));
        tp.add(new DefaultMapTeleport(44, new UID("2937963017-2545699419-2641637983-3828584092"),
                DefaultAct.ATLANTIS, "RECORDS\\XPACK3\\ITEMS\\SHRINES\\TELEPORT\\TELEPORTSHRINE_05_INNERATLANTIS.DBR"));

        /* East (DLC) */
        tp.add(new DefaultMapTeleport(45, new UID("3129847605-3490202547-2380758241-723818998"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINESANDYSHORES01.DBR"));
        tp.add(new DefaultMapTeleport(46, new UID("1405924112-873809342-3035070156-3862950636"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINELAIVILLAGE10.DBR"));
        tp.add(new DefaultMapTeleport(47, new UID("2894238962-2513192589-2872538379-869933337"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINESILKROAD02.DBR"));
        tp.add(new DefaultMapTeleport(48, new UID("2429796087-4202513727-2148846742-2700624635"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINEPINGYANG03.DBR"));
        tp.add(new DefaultMapTeleport(49, new UID("3797955852-3378203590-2591013785-1615468850"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINEEIGHTPILLARS04.DBR"));
        tp.add(new DefaultMapTeleport(50, new UID("2716059738-4107161326-2566288948-3177378579"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINETOPOFTHEWORLD08.DBR"));
        tp.add(new DefaultMapTeleport(51, new UID("1931105724-4036841229-2522653267-2325817371"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINEEGYPTCAMP05.DBR"));
        tp.add(new DefaultMapTeleport(52, new UID("3767095600-1567179805-3194534118-156020403"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINEEGYPTABYDOS09.DBR"));
        tp.add(new DefaultMapTeleport(53, new UID("3846349248-1895321370-2153346405-2862765892"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINEEGYPTCITY06.DBR"));
        tp.add(new DefaultMapTeleport(54, new UID("2083417698-3042987760-2257834914-3140259809"),
                DefaultAct.EAST, "RECORDS\\ITEM\\SHRINES\\TELEPORTCHINA\\TELEPORTSHRINEMARSHLAND07.DBR"));
    }

    public DefaultMapTeleport(int order, UID uid, DefaultAct act, String recordId) {
        super(order, uid, DefaultAct.get(act), recordId);
    }

    public static MapTeleport get(int order) {
        return tp.stream().filter(f -> f.getOrder() == order).findFirst().orElse(null);
    }

    public static MapTeleport get(UID uid) {
        return tp.stream().filter(f -> f.getUid().equals(uid)).findFirst().orElse(null);
    }

    public static MapTeleport get(String recordId) {
        return tp.stream().filter(f -> f.getRecordId().equals(recordId)).findFirst().orElse(null);
    }

    public static List<MapTeleport> getAll() {
        return tp;
    }
}
