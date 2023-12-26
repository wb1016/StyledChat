package eu.pb4.styledchat.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.EmptyNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.StaticPreParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.config.data.ChatStyleData;
import eu.pb4.styledchat.config.data.ConfigData;
import eu.pb4.styledchat.parser.DynamicNode;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ChatStyle {
    public static final ChatStyle EMPTY = new ChatStyle(new ChatStyleData());
    public static final NodeParser PARSER = NodeParser.merge(
            TextParserV1.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER,
            new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, DynamicNode::of),
            StaticPreParser.INSTANCE
    );
    public final MinecraftPredicate require;
    public final TextNode displayName;
    public final TextNode chat;
    public final TextNode join;
    public final TextNode joinFirstTime;
    public final TextNode joinRenamed;
    public final TextNode left;
    public final TextNode death;
    public final TextNode advancementTask;
    public final TextNode advancementChallenge;
    public final TextNode advancementGoal;
    public final TextNode privateMessageSent;
    public final TextNode privateMessageReceived;
    public final TextNode teamChatSent;
    public final TextNode teamChatReceived;
    public final TextNode sayCommand;
    public final TextNode meCommand;
    public final TextNode petDeath;
    public final TextNode spoilerStyle;
    public final String spoilerSymbol;
    public final TextNode linkStyle;
    public final TextNode mentionStyle;
    public final Map<String, TextNode> emoticons = new HashMap<>();
    public final Object2BooleanMap<String> formatting = new Object2BooleanOpenHashMap<>();
    public final Map<Identifier, TextNode> custom = new HashMap<>();

    public ChatStyle(ChatStyleData data, ChatStyle defaultStyle) {
        this.require = data instanceof ConfigData.RequireChatStyleData data1 ? data1.require : BuiltinPredicates.operatorLevel(0);

        this.displayName = data.displayName != null ? parseText(data.displayName) : defaultStyle.displayName;

        this.chat = data.messages.chat != null ? parseText(data.messages.chat) : defaultStyle.chat;
        this.join = data.messages.joinedGame != null ? parseText(data.messages.joinedGame) : defaultStyle.join;
        this.joinFirstTime = data.messages.joinedForFirstTime != null ? parseText(data.messages.joinedForFirstTime) : this.join;
        this.joinRenamed = data.messages.joinedAfterNameChange != null ? parseText(data.messages.joinedAfterNameChange) : defaultStyle.joinRenamed;
        this.left = data.messages.leftGame != null ? parseText(data.messages.leftGame) : defaultStyle.left;
        this.death = data.messages.baseDeath != null ? parseText(data.messages.baseDeath) : defaultStyle.death;
        this.advancementTask = data.messages.advancementTask != null ? parseText(data.messages.advancementTask) : defaultStyle.advancementTask;
        this.advancementChallenge = data.messages.advancementChallenge != null ? parseText(data.messages.advancementChallenge) : defaultStyle.advancementChallenge;
        this.advancementGoal = data.messages.advancementGoal != null ? parseText(data.messages.advancementGoal) : defaultStyle.advancementGoal;
        this.privateMessageSent = data.messages.privateMessageSent != null ? parseText(data.messages.privateMessageSent) : defaultStyle.privateMessageSent;
        this.privateMessageReceived = data.messages.privateMessageReceived != null ? parseText(data.messages.privateMessageReceived) : defaultStyle.privateMessageReceived;
        this.teamChatSent = data.messages.sentTeamChat != null ? parseText(data.messages.sentTeamChat) : defaultStyle.teamChatSent;
        this.teamChatReceived = data.messages.receivedTeamChat != null ? parseText(data.messages.receivedTeamChat) : defaultStyle.teamChatReceived;
        this.sayCommand = data.messages.sayCommandMessage != null ? parseText(data.messages.sayCommandMessage) : defaultStyle.sayCommand;
        this.meCommand = data.messages.meCommandMessage != null ? parseText(data.messages.meCommandMessage) : defaultStyle.meCommand;
        this.petDeath = data.messages.petDeathMessage != null ? parseText(data.messages.petDeathMessage) : defaultStyle.petDeath;

        this.spoilerStyle = data.spoilerStyle != null ? parseText(data.spoilerStyle) : defaultStyle.spoilerStyle;
        this.spoilerSymbol = data.spoilerSymbol != null ? data.spoilerSymbol : defaultStyle.spoilerSymbol;
        this.linkStyle = data.linkStyle != null ? parseText(data.linkStyle) : defaultStyle.linkStyle;
        this.mentionStyle = data.mentionStyle != null ? parseText(data.mentionStyle) : defaultStyle.mentionStyle;

        for (var emoticon : data.emoticons.entrySet()) {
            if (emoticon.getKey().startsWith("$")) {
                decodeSpecialEmoticon(emoticon.getKey(), emoticon.getValue());
            } else {
                this.emoticons.put(emoticon.getKey(), parseText(emoticon.getValue()));
            }
        }

        for (var formatting : data.formatting.entrySet()) {
            this.formatting.put(formatting.getKey(), formatting.getValue().booleanValue());
        }

        if (data.custom != null) {
            for (var entry : data.custom.entrySet()) {
                var id = Identifier.tryParse(entry.getKey());

                if (id != null) {
                    this.custom.put(id, parseText(entry.getValue()));
                }
            }
        }
    }

    public ChatStyle(ChatStyleData data) {
        this.require = data instanceof ConfigData.RequireChatStyleData data1 ? data1.require : BuiltinPredicates.operatorLevel(0);

        this.displayName = data.displayName != null ? parseText(data.displayName) : null;
        this.chat = data.messages.chat != null ? parseText(data.messages.chat) : null;
        this.join = data.messages.joinedGame != null ? parseText(data.messages.joinedGame) : null;
        this.joinRenamed = data.messages.joinedAfterNameChange != null ? parseText(data.messages.joinedAfterNameChange) : null;
        this.joinFirstTime = data.messages.joinedForFirstTime != null ? parseText(data.messages.joinedForFirstTime) : null;
        this.left = data.messages.leftGame != null ? parseText(data.messages.leftGame) : null;
        this.death = data.messages.baseDeath != null ? parseText(data.messages.baseDeath) : null;
        this.advancementTask = data.messages.advancementTask != null ? parseText(data.messages.advancementTask) : null;
        this.advancementChallenge = data.messages.advancementChallenge != null ? parseText(data.messages.advancementChallenge) : null;
        this.advancementGoal = data.messages.advancementGoal != null ? parseText(data.messages.advancementGoal) : null;
        this.privateMessageSent = data.messages.privateMessageSent != null ? parseText(data.messages.privateMessageSent) : null;
        this.privateMessageReceived = data.messages.privateMessageReceived != null ? parseText(data.messages.privateMessageReceived) : null;
        this.teamChatSent = data.messages.sentTeamChat != null ? parseText(data.messages.sentTeamChat) : null;
        this.teamChatReceived = data.messages.receivedTeamChat != null ? parseText(data.messages.receivedTeamChat) : null;
        this.sayCommand = data.messages.sayCommandMessage != null ? parseText(data.messages.sayCommandMessage) : null;
        this.meCommand = data.messages.meCommandMessage != null ? parseText(data.messages.meCommandMessage) : null;
        this.petDeath = data.messages.petDeathMessage != null ? parseText(data.messages.petDeathMessage) : null;

        this.spoilerStyle = data.spoilerStyle != null ? parseText(data.spoilerStyle) : null;
        this.spoilerSymbol = data.spoilerSymbol != null ? data.spoilerSymbol : null;
        this.linkStyle = data.linkStyle != null ? parseText(data.linkStyle) : null;
        this.mentionStyle = data.mentionStyle != null ? parseText(data.mentionStyle) : null;

        for (var emoticon : data.emoticons.entrySet()) {
            if (emoticon.getKey().startsWith("$")) {
                decodeSpecialEmoticon(emoticon.getKey(), emoticon.getValue());
            } else {
                this.emoticons.put(emoticon.getKey(), parseText(emoticon.getValue()));
            }
        }

        for (var formatting : data.formatting.entrySet()) {
            this.formatting.put(formatting.getKey(), formatting.getValue().booleanValue());
        }

        if (data.custom != null) {
            for (var entry : data.custom.entrySet()) {
                var id = Identifier.tryParse(entry.getKey());

                if (id != null) {
                    this.custom.put(id, parseText(entry.getValue()));
                }
            }
        }
    }

    private static TextNode parseText(String input) {
        return !input.isEmpty() ? PARSER.parseNode(input) : EmptyNode.INSTANCE;
    }

    private void decodeSpecialEmoticon(String baseKey, String baseValue) {
        var parts = baseKey.substring(1).split(":", 3);
        if (parts.length != 3) {
            return;
        }

        JsonObject json;

        if (parts[1].equals("from_file")) {
            json = ConfigManager.loadJson(parts[2]);
        } else if (parts[1].equals("builtin")) {
            json = ConfigManager.loadJsonBuiltin(parts[2]);
        } else {
            return;
        }

        if (parts[0].equals("default")) {
            try {
                for (var entry : json.entrySet()) {
                    this.emoticons.put(entry.getKey(),
                            NodeParser.merge(
                                    TextParserV1.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER,
                                    new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, (x) -> parseText(entry.getValue().getAsString())),
                                    StaticPreParser.INSTANCE
                            ).parseNode(baseValue)
                    );
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (parts[0].equals("emojibase") || parts[1].equals("emojibase_unlocked")) {
            try {
                var validate = parts[0].equals("emojibase");

                loopStart:
                for (var entry : json.entrySet()) {
                    var b = new StringBuilder();
                    for (var x : entry.getKey().split("-")) {
                        var i = Integer.parseInt(x, 16);

                        if (validate && ((i >= 0x1F3FB && i <= 0x1F3FF) || i == 0xFE0F || i == 0x200D)) {
                            continue loopStart;
                        }
                        b.appendCodePoint(Integer.parseInt(x, 16));
                    }

                    var output = NodeParser.merge(
                            TextParserV1.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER,
                            new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, (x) -> TextNode.of(b.toString())),
                            StaticPreParser.INSTANCE
                    ).parseNode(baseValue);

                    if (entry.getValue().isJsonArray()) {
                        for (var x : entry.getValue().getAsJsonArray()) {
                            this.emoticons.put(x.getAsString(), output);
                        }
                    } else {
                        this.emoticons.put(entry.getValue().getAsString(), output);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (parts[0].equals("cldr")) {
            try {
                json = json.getAsJsonObject("annotations").getAsJsonObject("annotations");

                for (var entry : json.entrySet()) {
                    try {
                        var value = NodeParser.merge(
                                TextParserV1.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER,
                                new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, (x) -> TextNode.of(entry.getKey())),
                                StaticPreParser.INSTANCE
                        ).parseNode(baseValue);
                        for (var key : entry.getValue().getAsJsonObject().getAsJsonArray("default")) {
                            this.emoticons.put(key.getAsString().replace(' ', '_').replace(':', '_'), value);
                        }
                    } catch (Throwable e) {

                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public Text getDisplayName(ServerPlayerEntity player, Text vanillaDisplayName) {
        if (this.displayName == null) {
            return null;
        } else if (this.displayName == EmptyNode.INSTANCE) {
            return vanillaDisplayName;
        }
        var context = PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("vanillaDisplayName", vanillaDisplayName, "player", vanillaDisplayName, "default", vanillaDisplayName, "name", player.getName()));

        return this.displayName.toText(context);
    }

    @Nullable
    public Text getChat(ServerPlayerEntity player, Text message) {
        if (this.chat == null) {
            return null;
        } else if (this.chat == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }


        return this.chat.toText(PlaceholderContext.of(player)
                .asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "message", message)));
    }

    @Nullable
    public Text getJoin(ServerPlayerEntity player) {
        if (this.join == null) {
            return null;
        } else if (this.join == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.join.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName())));
    }

    @Nullable
    public Text getJoinFirstTime(ServerPlayerEntity player) {
        if (this.joinFirstTime == null) {
            return null;
        } else if (this.joinFirstTime == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.joinFirstTime.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName())));
    }

    @Nullable
    public Text getJoinRenamed(ServerPlayerEntity player, String oldName) {
        if (this.joinRenamed == null) {
            return null;
        } else if (this.joinRenamed == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.joinRenamed.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "old_name", Text.literal(oldName))));
    }

    @Nullable
    public Text getLeft(ServerPlayerEntity player) {
        if (this.left == null) {
            return null;
        } else if (this.left == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.left.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName())));
    }

    @Nullable
    public Text getDeath(ServerPlayerEntity player, Text vanillaMessage) {
        if (this.death == null) {
            return null;
        } else if (this.death == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.death.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "default_message", vanillaMessage)));
    }

    @Nullable
    public Text getAdvancementGoal(ServerPlayerEntity player, Text advancement) {
        if (this.advancementGoal == null) {
            return null;
        } else if (this.advancementGoal == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.advancementGoal.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "advancement", advancement)));
    }

    @Nullable
    public Text getAdvancementTask(ServerPlayerEntity player, Text advancement) {
        if (this.advancementTask == null) {
            return null;
        } else if (this.advancementTask == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.advancementTask.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "advancement", advancement)));
    }

    @Nullable
    public Text getAdvancementChallenge(ServerPlayerEntity player, Text advancement) {
        if (this.advancementChallenge == null) {
            return null;
        } else if (this.advancementChallenge == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.advancementChallenge.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "advancement", advancement)));
    }

    @Nullable
    public Text getSayCommand(ServerCommandSource source, Text message) {
        if (this.sayCommand == null) {
            return null;
        } else if (this.sayCommand == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.sayCommand.toText(PlaceholderContext.of(source).asParserContext().with(DynamicNode.NODES, Map.of("player", source.getDisplayName(), "displayName", source.getDisplayName(), "message", message)));
    }

    @Nullable
    public Text getMeCommand(ServerCommandSource source, Text message) {
        if (this.meCommand == null) {
            return null;
        } else if (this.meCommand == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.meCommand.toText(PlaceholderContext.of(source).asParserContext().with(DynamicNode.NODES, Map.of("player", source.getDisplayName(), "displayName", source.getDisplayName(), "message", message)));

    }

    @Nullable
    public Text getPrivateMessageSent(Text sender, Text receiver, Text message, PlaceholderContext context) {
        if (this.privateMessageSent == null) {
            return null;
        } else if (this.privateMessageSent == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.privateMessageSent.toText(context.asParserContext().with(DynamicNode.NODES, Map.of("sender", sender, "receiver", receiver, "message", message)));
    }

    @Nullable
    public Text getPrivateMessageReceived(Text sender, Text receiver, Text message, PlaceholderContext context) {
        if (this.privateMessageReceived == null) {
            return null;
        } else if (this.privateMessageReceived == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.privateMessageReceived.toText(context.asParserContext().with(DynamicNode.NODES, Map.of("sender", sender, "receiver", receiver, "message", message)));
    }

    @Nullable
    public Text getTeamChatSent(Text team, Text displayName, Text message, ServerCommandSource context) {
        if (this.teamChatSent == null) {
            return null;
        } else if (this.teamChatSent == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.teamChatSent.toText(PlaceholderContext.of(context).asParserContext().with(DynamicNode.NODES, Map.of("team", team, "displayName", displayName, "message", message)));
    }

    @Nullable
    public Text getTeamChatReceived(Text team, Text displayName, Text message, ServerCommandSource context) {
        if (this.teamChatReceived == null) {
            return null;
        } else if (this.teamChatReceived == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return this.teamChatReceived.toText(PlaceholderContext.of(context).asParserContext().with(DynamicNode.NODES, Map.of("team", team, "displayName", displayName, "message", message)));
    }

    @Nullable
    public Text getCustom(Identifier identifier, Text displayName, Text message, @Nullable Text receiver, ServerCommandSource source) {
        var node = this.custom.get(identifier);

        if (node == null) {
            return null;
        } else if (node == EmptyNode.INSTANCE) {
            return StyledChatUtils.IGNORED_TEXT;
        }

        return node.toText(PlaceholderContext.of(source).asParserContext().with(DynamicNode.NODES, Map.of("receiver", receiver == null ? Text.empty() : receiver, "displayName", displayName, "message", message)));
    }

    @Nullable
    public TextNode getLink() {
        return this.linkStyle;
    }

    @Nullable
    public TextNode getMention() {
        return this.mentionStyle;
    }

    @Nullable
    public TextNode getSpoilerStyle() {
        return this.spoilerStyle;
    }

    @Nullable
    public String getSpoilerSymbol() {
        return this.spoilerSymbol;
    }

    public Text getPetDeath(TameableEntity entity, Text vanillaMessage) {
        if (this.petDeath == null) {
            return null;
        }

        return this.petDeath.toText(PlaceholderContext.of(entity).asParserContext().with(DynamicNode.NODES, Map.of("pet", entity.getDisplayName(), "default_message", vanillaMessage)));
    }
}
