package com.example.campshare.admin;
public class GearCannotBeDeletedException extends RuntimeException { public GearCannotBeDeletedException(){super("予約履歴があるため削除できません。");} }
