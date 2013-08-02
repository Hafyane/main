<ul class="pageitem">
  <li class="textbox">
    <b>{$list->title|escape:"html"}</b>{if $list->description}: <i>{$list->description|escape}</i>{/if}
  </li>
  {foreach from=$resourceList item=resource name="recordLoop"}
    {* This is raw HTML -- do not escape it: *}
    {$resource}
  {foreachelse}
    <li class="textbox">{translate text='You do not have any saved resources'}</li>
  {/foreach}
  {if $pageLinks.all}
    <li class="autotext"><div class="pagination">{$pageLinks.all}</div></li>
  {/if}
</ul>

{include file="MyResearch/menu.tpl"}
