/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.jagrosh.jmusicbot.commands;

import me.jagrosh.jdautilities.commandclient.CommandEvent;
import me.jagrosh.jmusicbot.Bot;
import me.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkipCmd extends MusicCommand {

    public SkipCmd(Bot bot)
    {
        super(bot);
        this.name = "skip";
        this.help = "votes to skip the current song";
        this.aliases = new String[]{"voteskip"};
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(event.getAuthor().getId().equals(handler.getCurrentTrack().getIdentifier()))
        {
            event.reply(event.getClient().getSuccess()+" Skipped **"+handler.getCurrentTrack().getTrack().getInfo().title
                    +"** (requested by **"+event.getAuthor().getName()+"**)");
            handler.getPlayer().stopTrack();
        }
        else
        {
            int listeners = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if(handler.getVotes().contains(event.getAuthor().getId()))
                msg = event.getClient().getWarning()+" You already voted to skip this song `[";
            else
            {
                msg = event.getClient().getSuccess()+" You voted to skip the song `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            int skippers = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            int required = (int)Math.ceil(listeners * .55);
            msg+= skippers+" votes, "+required+"/"+listeners+" needed]`";
            if(skippers>=required)
            {
                User u = event.getJDA().getUserById(handler.getCurrentTrack().getIdentifier());
                msg+="\n"+event.getClient().getSuccess()+" Skipped **"+handler.getCurrentTrack().getTrack().getInfo().title
                    +"** (requested by "+(u==null ? "someone" : "**"+u.getName()+"**")+")";
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }
    
}
